package com.gu.integration.test.features

import com.gu.automation.support.TestLogging
import org.openqa.selenium.WebDriver
import com.gu.integration.test.pages.article.ArticlePage
import com.gu.fronts.integration.test.PageLoader._
import org.scalatest.Matchers

case class ArticleSteps(implicit driver: WebDriver) extends TestLogging with Matchers {

  def goToArticle(relativeArticleUrl: String): ArticlePage = {
    logger.step(s"I am an Article page with relative url: $relativeArticleUrl")
    lazy val article = new ArticlePage()
    goTo(fromRelativeUrl(relativeArticleUrl), article)
  }

  def checkMostPopularDisplayedProperly(articlePage: ArticlePage) = {
    logger.step("Get most popular module and check that it is properly displayed")
    articlePage.mostPopularModule.displayedLinks should not be empty
    articlePage.mostPopularModule.displayedImages should not be empty
  }
  
  def checkMostRelatedContentDisplayedProperly(articlePage: ArticlePage) = {
    logger.step("Get related content module and check that it is properly displayed")
    articlePage.relatedContentModule.displayedLinks should not be empty
    articlePage.relatedContentModule.displayedImages should not be empty
  }
}
