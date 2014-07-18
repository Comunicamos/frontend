package frontpress

import com.amazonaws.regions.{Regions, Region}
import com.amazonaws.services.sqs.AmazonSQSAsyncClient
import common.{Logging, Message, FaciaPressMetrics, JsonMessageQueue}
import common.SQSQueues._
import conf.Configuration
import org.joda.time.DateTime
import services.{Live, Draft, FrontPath, PressJob}

import scala.concurrent.Future
import scala.util.{Success, Failure}

object ToolPressQueueWorker extends JsonQueueWorker[PressJob] with Logging {
  override val queue = (Configuration.faciatool.frontPressToolQueue map { queueUrl =>
    JsonMessageQueue[PressJob](
      new AmazonSQSAsyncClient(Configuration.aws.credentials).withRegion(Region.getRegion(Regions.EU_WEST_1)),
      queueUrl
    )
  }) getOrElse {
    throw new RuntimeException("Required property 'frontpress.sqs.tool_queue_url' not set")
  }

  /** We record separate metrics for each of the editions' network fronts */
  val metricsByPath = Map(
    "uk" -> FaciaPressMetrics.UkFrontPressLatency,
    "us" -> FaciaPressMetrics.UsFrontPressLatency,
    "au" -> FaciaPressMetrics.AuFrontPressLatency
  )

  override def process(message: Message[PressJob]): Future[Unit] = {
    val PressJob(FrontPath(path), pressType) = message.get

    log.info(s"Processing job from tool to update $path on $pressType")

    val pressFuture = pressType match {
      case Draft => FrontPress.pressDraftByPathId(path)
      case Live => FrontPress.pressLiveByPathId(path)
    }

    pressFuture onComplete {
      case Success(_) =>
        pressType match {
          case Draft => FaciaPressMetrics.FrontPressDraftSuccess.increment()
          case Live => FaciaPressMetrics.FrontPressLiveSuccess.increment()
        }

        val maybeElapsed = message.sentAt.map(DateTime.now.getMillis - _.getMillis)

        maybeElapsed match {
          case Some(millisToPress) =>
            if (millisToPress < 0) {
              log.error(s"Tachyons messing up our pressing! (pressed in ${millisToPress}ms)")
            } else {
              FaciaPressMetrics.FrontPressLatency.recordTimeSpent(millisToPress)

              metricsByPath.get(path) foreach { metric =>
                metric.recordTimeSpent(millisToPress)
              }
            }

            log.info(s"Successfully pressed $path on $pressType after ${millisToPress}ms")

          case None =>
            log.error("Asked SQS for SentTimestamp but did not receive it")
            log.info(s"Successfully pressed $path on $pressType")
        }

      case Failure(error) =>
        pressType match {
          case Draft => FaciaPressMetrics.FrontPressDraftFailure.increment()
          case Live => FaciaPressMetrics.FrontPressLiveFailure.increment()
        }
        log.error(s"Failed to press $path on $pressType", error)
    }

    pressFuture.map(_ => ())
  }
}
