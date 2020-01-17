package org.newsroom

import java.nio.charset.StandardCharsets

import ujson.Value.Value
import scalaj.http.{Http, HttpConstants, HttpResponse, Token}

import scala.util._

object NewsApiCrawler extends App {

  getTopTrendingFr

  def getTopTrendingFr = {
    parse(httpCall("https://newsapi.org/v2/top-headlines"))("articles")
      .arr
      .map(json => {
        Article(
          source = parseSource(json("source")),
          title = parseField(json("title")),
          description = parseField(json("description")),
          url = parseField(json("url")),
          urlToImage = parseField(json("urlToImage")),
          publishedAt = parseField(json("publishedAt")),
          content = parseField(json("content"))
        )
      }).map(value => println(value.source))
  }

  /**
   *
   * @param field
   * @return
   */
  def parseField(field: Value): String = Try {
    field.str
  } match {
    case Success(value) => value
    case Failure(_) => ""
  }

  /**
   *
   * @param field
   * @return
   */
  def parseSource(field: Value): String = Try {
    field("name").str
  } match {
    case Success(value) => value
    case Failure(_) => ""
  }

  /**
   *
   * @param json
   * @return
   */
  def parse(json: String): Value = ujson.read(json)

  /**
   *
   * @param title
   * @param description
   * @param url
   * @param urlToImage
   * @param publishedAt
   * @param content
   */
  case class Article(
                      source: String,
                      title: String,
                      description: String,
                      url: String,
                      urlToImage: String,
                      publishedAt: String,
                      content: String
                    )


  implicit class StringImplicit(value: String) {
    def encodingToUtf8: String = new String(value.getBytes(StandardCharsets.UTF_8))
  }

  /**
   *
   * @param url
   * @return
   */
  def httpCall(url: String): String = Http(url)
    .charset(HttpConstants.utf8)
    .param("apiKey", "")
    .param("q", "")
    .param("country", "fr")
    .charset(HttpConstants.utf8)
    .asString
    .body

}
