package org.newsroom.eslastic

import java.util.Date

import org.newsroom.eslastic.action.EsActionImpl
import org.newsroom.logger.LogsHelper
import org.newsroom.schema.ArticleMetaData
import zio.Task

import scala.concurrent.Future

object ESIndexer extends LogsHelper {

  val INDEX_NAME: String = "articles3"
  val ES_URL: String = "http://localhost:9200/"


  /* Run Refacto Nouvelle Index */
  def indexArticles(articles: Seq[ArticleMetaData]): Seq[Task[Future[ArticleId]]] = {
    articles map (article => EsActionImpl.addDocFuture(article))
  }

  /* Run Refacto Nouvelle Index */
  def indexArticle(article: ArticleMetaData): Task[Future[ArticleId]] = {
    EsActionImpl.addDocFuture(article)
  }
}


/* Case Class */
case class ArticleId(id: String)
case class ArticleMetaDataWithId(id: String, title: String, url: String, publishedDate: Date, author: String, description: String)





