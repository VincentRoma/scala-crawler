package org.newsroom.eslastic

import java.nio.charset.StandardCharsets

import org.newsroom.logger.LogsHelper
import org.newsroom.rss.RSSScrapper.ArticleMetaData
import org.newsroom.utils.DateUtils
import java.security.MessageDigest

import scala.util.{Failure, Success, Try}

class ESIndexer(articleMetaDataSeq: Seq[ArticleMetaData]) extends LogsHelper {

  import ESIndexer._

  /**
   *
   * @return
   */
  def run = {
    logger.info(s"[RSS] - Indexing ${articleMetaDataSeq.size} articles")
    articleMetaDataSeq
      .foreach(postDocument("articles", _)
      match {
        case Success(_) => logger.info("[Success]")
        case Failure(ex) => logger.info(s"[Error] : ${ex} ")
      })
  }


  /**
   *
   * @param article
   * @return
   */
  def generateJson(article: ArticleMetaData) = {
    val json =
      s"""{
         |  "title": "${article.title}",
         |  "url": "${article.url}",
         |  "publishedDate": "${DateUtils.getDateAsString(article.publishedDate, Some("yyyy-MM-dd"))}",
         |  "author" : "${article.author}",
         |  "description": "${
        encodingToUtf8(article.description)
          .replaceAll("\\n", "")
          .replaceAll("\"", "")
          .trim
      }"
         |  }
         |""".stripMargin
    json
  }

  def encodingToUtf8(value: String): String = new String(value.getBytes(StandardCharsets.UTF_8))

  def md5Hash(text: String) : String = {
    java.security.MessageDigest.getInstance("MD5").digest(text.getBytes()).map(0xFF & _).map{
      "%02x".format(_)
    }.foldLeft(""){_ + _}
  }

  /**
   *
   * @param indexName
   * @param data
   * @return
   */
  def updateDocument(indexName: String, data: ArticleMetaData) = {
    logger.info(s"[ES] - Indexing ${data.url} ")
    Try {
      requests.post(ES_URL + indexName + "/_update/" + data.url, headers = Map("content-type" -> "application/json"), data = generateJson(data))
    }
  }

  def postDocument(indexName: String, data: ArticleMetaData) = {
    logger.info(s"[ES] - Indexing ${data.url} ")
    Try {
      val id = md5Hash(data.url)
      val response = requests.post(ES_URL + indexName + "/_doc/" + id, headers = Map("content-type" -> "application/json"), data = generateJson(data))
      logger.info(s"[ES] - Elastic Response - id:" + id + " - " + response.statusCode)
    }
  }
}

object ESIndexer {
  def apply(articleMetaDataSeq: Seq[ArticleMetaData]): ESIndexer = new ESIndexer(articleMetaDataSeq)

  val ES_URL: String = "http://localhost:9200/"
}
