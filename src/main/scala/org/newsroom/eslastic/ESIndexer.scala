package org.newsroom.eslastic

import java.nio.charset.StandardCharsets

import org.newsroom.logger.LogsHelper
import org.newsroom.rss.RSSScrapper.ArticleMetaData
import org.newsroom.utils.DateUtils

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
    println(json)
    json
  }

  def encodingToUtf8(value: String): String = new String(value.getBytes(StandardCharsets.UTF_8))


  /**
   *
   * @param indexName
   * @param data
   * @return
   */
  def updateDocument(indexName: String, data: ArticleMetaData) = {
    logger.info(s"[RSS] - Indexing ${data.title} ")
    Try {
      requests.post(ES_URL + indexName + "/_update/" + data.url, headers = Map("content-type" -> "application/json"), data = generateJson(data))
    }
  }

  def postDocument(indexName: String, data: ArticleMetaData) = {
    logger.info(s"[RSS] - Indexing ${data.title} ")
    Try {
      requests.post(ES_URL + indexName + "/_doc", headers = Map("content-type" -> "application/json"), data = generateJson(data))
    }
  }
}

object ESIndexer {
  def apply(articleMetaDataSeq: Seq[ArticleMetaData]): ESIndexer = new ESIndexer(articleMetaDataSeq)

  val ES_URL: String = "http://localhost:9200/"
}
