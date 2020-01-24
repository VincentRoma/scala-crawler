package org.newsroom.eslastic.action

import org.newsroom.eslastic.{ArticleId, ArticleMetaDataWithId, ESIndexer}
import org.newsroom.schema.ArticleMetaData
import org.newsroom.utils.{DateUtils, HashUtils}
import requests.Response
import zio.Task

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try



//type TaskDB[A] = ZIO[Database, Throwable, A]



trait EsAction {

  def addDocTry(article: ArticleMetaData): Task[Try[ArticleId]]

  def addDocFuture(article: ArticleMetaData): Task[Future[ArticleId]]

  def updateDocTry(id: ArticleMetaDataWithId): Task[Try[ArticleId]]

  def updateDocFuture(id: ArticleMetaDataWithId): Task[Future[ArticleId]]

  def getDocs(): Task[List[ArticleMetaDataWithId]]
}


// Implementation
object EsActionImpl extends EsAction {

  import ESIndexer._

  /* AddDocTry */
  override def addDocTry(article: ArticleMetaData): Task[Try[ArticleId]] = Task {
    for {
      id <- postDocumentTry(article.url, "jsonData")
    } yield {
      ArticleId(id)
    }
  }

  // TRY IMPLEMENT
  def postDocumentTry(url: String, data: String): Try[String] = {
    for {
      id <- HashUtils.md5HashTry(url) // Generate md5Hash
      e <- postTry(id, data) // Post to ES
    } yield {
      logger.info(s"[RSS] - Success Indexing id $id")
      id
    }
  }

  // POST TRY
  def postTry(id: String, data: String) = Try {
    requests.post(ES_URL + INDEX_NAME + "/_doc/" + id, headers = Map("content-type" -> "application/json"), data = data)
  }

  /* AddFuture */
  override def addDocFuture(article: ArticleMetaData): Task[Future[ArticleId]] = Task {
    for {
      id <- postDocumentFuture(article.url, "jsonData")
    } yield {
      ArticleId(id)
    }
  }

  /* UpdateTry */
  override def updateDocTry(article: ArticleMetaDataWithId): Task[Try[ArticleId]] = Task {
    for {
      id <- postDocumentTry(article.url, "jsonData") // Todo : Update Document
    } yield {
      ArticleId(id)
    }
  }

  /* UpdateFuture */
  override def updateDocFuture(article: ArticleMetaDataWithId): Task[Future[ArticleId]] = Task {
    for {
      id <- postDocumentFuture(article.url, "jsonData") // Todo : Update Document
    } yield {
      ArticleId(id)
    }
  }

  // FUTURE IMPLEMENT
  def postDocumentFuture(url: String, data: String): Future[String] = {
    for {
      id <- HashUtils.md5HashFuture(url) // Generate md5Hash
      e <- postFuture(id, data) // Post to ES
    } yield {
      logger.info(s"[RSS] - Success Indexing id $id")
      id
    }
  }

  // POST FUTURE
  def postFuture(id: String, data: String): Future[Response] = Future {
    requests.post(ES_URL + INDEX_NAME + "/_doc/" + id, headers = Map("content-type" -> "application/json"), data = data)
  }

  override def getDocs(): Task[List[ArticleMetaDataWithId]] = Task {
    List(ArticleMetaDataWithId("", "", "", DateUtils.convertStringToDate("1900-01-01", Some("yyyy-mm-dd")), "", ""))
  }

  def getJson(article: ArticleMetaData): Try[String] = Try {
    s"""{
       |  "title": "${article.title}",
       |  "url": "${article.url}",
       |  "publishedDate": "${DateUtils.getDateAsString(article.publishedDate, Some("yyyy-MM-dd"))}",
       |  "author" : "${article.author}",
       |  "description": "${
      HashUtils.encodingToUtf8(article.description)
        .replaceAll("\\n", "")
        .replaceAll("\"", "")
        .trim
    }"
       |  }
       |""".stripMargin
  }

}






