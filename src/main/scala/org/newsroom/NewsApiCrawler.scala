package org.newsroom

import java.nio.charset.StandardCharsets
import java.nio.charset.StandardCharsets
import org.newsroom.utils.DateUtils
import ujson.Value.Value
import scalaj.http.{Http, HttpConstants, HttpResponse, Token}

import scala.util._

object NewsApiCrawler extends App {


  import HttpParam._


  getTopTrendingFr




  def getTopTrendingFr = {
    parse(httpCall("https://newsapi.org/v2/top-headlines", "fr", httpParamTrendingsFr))("articles")
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
      }).map(value => println(value.url))
  }

  def getAllFr = {
    parse(httpCall("https://newsapi.org/v2/everything", "fr",httpParamTrendingsFr))("articles")
      .arr
      .foreach(json => {
        //        Article(
        //          source = parseSource(json("source")),
        //          title = parseField(json("title")),
        //          description = parseField(json("description")),
        //          url = parseField(json("url")),
        //          urlToImage = parseField(json("urlToImage")),
        //          publishedAt = parseField(json("publishedAt")),
        //          content = parseField(json("content"))
        //        )
        println(json)
      })
  }


  def getSourcesFr = {
    parse(httpCall("https://newsapi.org/v2/sources", "fr",httpParamTrendingsFr))("sources")
      .arr
      .foreach(json => {
        //        Article(
        //          source = parseSource(json("source")),
        //          title = parseField(json("title")),
        //          description = parseField(json("description")),
        //          url = parseField(json("url")),
        //          urlToImage = parseField(json("urlToImage")),
        //          publishedAt = parseField(json("publishedAt")),
        //          content = parseField(json("content"))
        //        )
        println(json)
      })
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
  def httpCall(url: String, country: String, httpParam: Seq[(String, String)]): String = {
    val a = Http(url)
      .charset(HttpConstants.utf8)
      .param("apiKey", "fc97a9aded994810a43cac97199c896c")
      .params(httpParam)
      .charset(HttpConstants.utf8)
      .asString
      .body


    print(a)
    a

  }

  object HttpParam {
    lazy val httpParamTrendingsFr: Seq[(String, String)] = Seq(
      ("pageSize", "100"),
      ("language", "fr"),
      ("page","1"),
      ("from",DateUtils.getTime(-1)),
      ("to",DateUtils.getTime(0))
    )
  }

  //  override def run(args: List[String]): ZIO[NewsApiCrawler.Environment, Nothing, Int] = ???
}
