package com.gu.integration.test.util

import scala.collection.JavaConverters.asScalaBufferConverter

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.SearchContext
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement


object ElementLoader {

  val TestAttributeName = "data-test-id"

  /**
   * Will find the element with the provided test attribute id and, if provided, using the provided webelement as search context
   * otherwise it will use the WebDriver, which has to be in scope
   */
  def findByTestAttribute(testAttributeValue: String, contextElement: Option[SearchContext] = None)(implicit driver: WebDriver): WebElement = {
    contextElement.getOrElse(driver).findElement(byTestAttributeId(testAttributeValue))
  }

  /**
   * Will find all elements with the provided test attribute id and, if provided, using the provided webelement as search context
   * otherwise it will use the WebDriver, which has to be in scope
   */
  def findAllByTestAttribute(testAttributeValue: String, contextElement: Option[SearchContext] = None)(implicit driver: WebDriver): List[WebElement] = {
    contextElement.getOrElse(driver).findElements(byTestAttributeId(testAttributeValue)).asScala.toList
  }

  private def byTestAttributeId(testAttributeValue: String): org.openqa.selenium.By = {
    By.cssSelector(s"[$TestAttributeName=$testAttributeValue]")
  }

  /**
   * Find all link elements, including nested, from the provided SearchContext and returns those that are displayed
   */
  def displayedLinks(searchContext: SearchContext): List[WebElement] = {
    searchContext.findElements(By.cssSelector("a")).asScala.toList.filter(element => element.isDisplayed)
  }

  /**
   * Find all image elements, including nested, from the provided SearchContext and returns those that are displayed.
   * Observe that this method does a double check as the selenium isDisplayed method does not guarantee that a picture is actually
   * visible
   */
  def displayedImages(searchContext: SearchContext)(implicit driver: WebDriver): List[WebElement] = {
    val preDisplayedImages = searchContext.findElements(By.cssSelector("img")).asScala.toList.filter(element => element.isDisplayed)
    //preDisplayedImages
    return preDisplayedImages.filter(element => isImageDisplayed(element))
  }

  /**
   * Use this method to check that an img element is properly displayed. This is needed as the Selenium isDisplayed does not explicitly
   * check that the image is displayed, just that the element is there and visible. This actually checks the size of the image to
   * make sure it is greater than 0
   */
  def isImageDisplayed(imageElement: WebElement)(implicit driver: WebDriver): Boolean = {
    val result = driver.asInstanceOf[JavascriptExecutor].
      executeScript("return arguments[0].complete && typeof arguments[0].naturalWidth != \"undefined\" && arguments[0].naturalWidth > 0",
        imageElement)
    if (result.isInstanceOf[java.lang.Boolean]) {
      result.asInstanceOf[java.lang.Boolean]
    } else {
      false
    }
  }
}