package views.support

import model.{FaciaImageElement, Trail}

object CutOut {
  def fromTrail(trail: Trail): Option[CutOut] = {
    if (trail.isComment || trail.imageCutoutReplace) {
      trail.customImageCutout map { case FaciaImageElement(src, _, _) => CutOut("", src) } orElse {
        for {
          contributor <- trail.contributors.find(_.contributorLargeImagePath.isDefined)
          imagePath <- contributor.contributorLargeImagePath
        } yield CutOut(contributor.name, imagePath)
      }
    } else {
      None
    }
  }
}

case class CutOut(altText: String, imageUrl: String)