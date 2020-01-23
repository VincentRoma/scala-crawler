package org.newsroom.eslastic

import java.nio.charset.StandardCharsets

import org.newsroom.logger.LogsHelper
import org.newsroom.rss.RSSScrapper.ArticleMetaData
import org.newsroom.utils.DateUtils
import java.security.MessageDigest

import scala.util.{Failure, Success, Try}

object ESIndexer extends LogsHelper {

  val INDEX_NAME: String = "articles3"
  val ES_URL: String = "http://localhost:9200/"


  def index(articles: Seq[ArticleMetaData]) = {
    articles map (article => {
      val fP = for {
        data <- getJson(article)
        id <- postDocument(article.url, data)
      } yield {
        id
      }
      fP
    })
  }

  def getJson(article: ArticleMetaData): Try[String] = Try {
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
  }

  def encodingToUtf8(value: String): String = new String(value.getBytes(StandardCharsets.UTF_8))

  def md5Hash(text: String): Try[String] = Try {
    java.security.MessageDigest.getInstance("MD5").digest(text.getBytes()).map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") {
      _ + _
    }
  }

  def postDocument(url: String, data: String): Try[String] = {
    for {
      id <- md5Hash(url) // Generate md5Hash
      e <- postEs(id, data) // Post to ES
    } yield {
      logger.info(s"[RSS] - Success Indexing id $id")
      id
    }
  }

  def postEs(id: String, data: String) = Try {
    requests.post(ES_URL + INDEX_NAME + "/_doc/" + id, headers = Map("content-type" -> "application/json"), data = data)
  }
}
