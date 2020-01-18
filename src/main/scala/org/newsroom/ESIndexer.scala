package org.newsroom

import com.sun.net.httpserver.Authenticator.Success
import org.newsroom.RSSScrapper.ArticleMetaData
import scalaj.http.{Http, HttpConstants}
import requests._

import scala.util.Try


class ESIndexer(articleMetaDataSeq: Seq[ArticleMetaData]) {

  import ESIndexer._


  /**
   *
   * @return
   */
  def run = {
    println(s"Indexing ${articleMetaDataSeq.size}")
    articleMetaDataSeq.map(httpPut("lemonde", _))
  }


  /**
   *
   * @param article
   * @return
   */
  def generateJson(article: ArticleMetaData) = {
    s"""{
       |  "title": "${article.title}",
       |  "url": "${article.url}",
       |  "publishedDate": "${article.publishedDate}",
       |  "author" : "${article.author}",
       |  "description": "${article.description}"
       |  }
       |""".stripMargin
  }

  /**
   *
   * @param indexName
   * @param data
   * @return
   */
  def httpPut(indexName: String, data: ArticleMetaData) = {
    println(s"index ${data.title}")
    requests.post(ES_URL + indexName + "/_doc", headers = Map("content-type" -> "application/json"), data = generateJson(data))
  }
}

object ESIndexer {
  def apply(articleMetaDataSeq: Seq[ArticleMetaData]): ESIndexer = new ESIndexer(articleMetaDataSeq)

  val ES_URL: String = "http://localhost:9200/"
}
