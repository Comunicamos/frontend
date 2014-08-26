package model

import common.Edition
import dfp.DfpAgent
import play.api.libs.json.{JsString, JsValue}

case class FaciaPage(
                      id: String,
                      seoData: SeoData,
                      collections: List[(Config, Collection)]) extends MetaData with AdSuffixHandlingForFronts {

  override lazy val description: Option[String] = seoData.description
  override lazy val section: String = seoData.navSection
  lazy val navSection: String = section
  override lazy val analyticsName: String = s"GFE:${seoData.webTitle.capitalize}"
  override lazy val webTitle: String = seoData.webTitle
  override lazy val title: Option[String] = seoData.title

  override lazy val isFront = true

  override lazy val metaData: Map[String, JsValue] = super.metaData ++ faciaPageMetaData
  lazy val faciaPageMetaData: Map[String, JsValue] = newMetaData

  lazy val newMetaData: Map[String, JsValue] = Map(
    "keywords" -> JsString(webTitle.capitalize),
    "contentType" -> JsString(contentType)
  )

  val isNetworkFront: Boolean = Edition.all.exists(edition => id.toLowerCase.endsWith(edition.id.toLowerCase))

  override lazy val contentType: String =   if (isNetworkFront) GuardianContentTypes.NETWORK_FRONT else GuardianContentTypes.SECTION

  override def isSponsored = DfpAgent.isSponsored(id)
  override def isAdvertisementFeature = DfpAgent.isAdvertisementFeature(id)
  override def sponsor = DfpAgent.getSponsor(id)
  override def hasPageSkin(edition: Edition) = DfpAgent.isPageSkinned(adUnitSuffix, edition)

  def allItems = collections.map(_._2).flatMap(_.items).distinct
}

object FaciaPage {
  def defaultFaciaPage: FaciaPage = FaciaPage("", SeoData.empty, Nil)
}
