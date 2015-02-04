package controllers

import com.gu.contentapi.client.model.ItemResponse
import common._
import conf.Configuration.commercial.expiredAdFeatureUrl
import conf.LiveContentApi.getResponse
import conf.Switches.AdFeatureExpirySwitch
import conf._
import model._
import play.api.mvc._
import views.support.RenderOtherStatus

import scala.concurrent.Future

case class InteractivePage (interactive: Interactive, related: RelatedContent)

object InteractiveController extends Controller with Logging with ExecutionContexts {

  def renderInteractiveJson(path: String): Action[AnyContent] = renderInteractive(path)
  def renderInteractive(path: String): Action[AnyContent] = Action.async { implicit request =>

    lookup(path) map {
      case Left(model) if model.interactive.isExpired => RenderOtherStatus(Gone) // TODO - delete this line after switching to new content api
      case Left(model) => render(model)
      case Right(other) => RenderOtherStatus(other)
    }
  }

  private def lookup(path: String)(implicit request: RequestHeader): Future[Either[InteractivePage, Result]] = {
    val edition = Edition(request)
    log.info(s"Fetching interactive: $path for edition $edition")
    val response: Future[ItemResponse] = getResponse(
      LiveContentApi.item(path, edition)
       .showFields("all")
    )

    val result = response map { response =>
      val interactive = response.content map { Interactive(_) }
      val page = interactive.map(i => InteractivePage(i, RelatedContent(i, response)))

      if (AdFeatureExpirySwitch.isSwitchedOn &&
        interactive.exists(_.isExpiredAdvertisementFeature)) {
        Right(MovedPermanently(expiredAdFeatureUrl))
      } else {
        ModelOrResult(page, response)
      }
    }

    result recover convertApiExceptions
  }


  private def render(model: InteractivePage)(implicit request: RequestHeader) = {
    val htmlResponse = () => views.html.interactive(model)
    val jsonResponse = () => views.html.fragments.interactiveBody(model)
    renderFormat(htmlResponse, jsonResponse, model.interactive, Switches.all)
  }
}
