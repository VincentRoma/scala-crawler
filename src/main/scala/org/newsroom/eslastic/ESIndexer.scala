package org.newsroom.eslastic

import org.newsroom.logger.LogsHelper
import org.newsroom.rss.RSSScrapper.ArticleMetaData

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
      .foreach(updateDocument("lemonde", _)
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
  def updateDocument(indexName: String, data: ArticleMetaData) = {
    logger.info(s"[RSS] - Indexing ${data.title} ")
    Try {
      requests.post(ES_URL + indexName + "/_update/" + data.url, headers = Map("content-type" -> "application/json"), data = generateJson(data))
    }
  }
}

object ESIndexer {
  def apply(articleMetaDataSeq: Seq[ArticleMetaData]): ESIndexer = new ESIndexer(articleMetaDataSeq)

  val ES_URL: String = "http://localhost:9200/"
}
