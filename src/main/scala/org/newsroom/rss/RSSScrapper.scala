package org.newsroom.rss

import java.io.BufferedWriter
import java.net.URL

import com.rometools.rome.feed.synd.{SyndEntry, SyndFeed}
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import org.newsroom.eslastic.ESIndexer._
import org.newsroom.logger.LogsHelper
import org.newsroom.schema.ArticleMetaData
import org.newsroom.utils.FileUtils._
import zio.{ IO, Task, ZIO}

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

object RSSScrapper extends App with LogsHelper {

  run


  def run: ZIO[Any, Throwable, Seq[Try[ZIO[Any, Throwable, Unit]]]] = {
    for {
      rssUrls <- rssUrlsReader
      filterI <- filterIdReader
      idWriter <- bufferWriter
    } yield {
      scrap(rssUrls, filterI, idWriter)
    }
  }


  /**
   *
   * @param rssList
   * @return
   */
  def scrap(rssList: Seq[String],
            seqId: Seq[String],
            initWriter: BufferedWriter): Seq[Try[ZIO[Any, Throwable, Unit]]] = {
    rssList.map(rss => {
      for {
        (name, flux) <- parseRssRow(rss)
      } yield for {
        data <- initRssSync(flux)
        articlesFullSeq <- parse(data, name)
        articlesFilterSeq <- filterByOldId(articlesFullSeq, seqId)
        idFuturSeq <- indexArticles(articlesFilterSeq)
        isSucces <- writeFile(initWriter, idFuturSeq)
      } yield {
        logger.info(s"index $isSucces")
      }
    })
  }

  /**
   *
   * @param row
   * @return
   */
  def parseRssRow(row: String) = Try {
    val name = row.split("\";\"")(0)
    val flux = row.split("\";\"")(1)
    (name.substring(1), flux)
  }


  def parse(syndEntry: mutable.Buffer[SyndEntry], name: String): Task[mutable.Buffer[ArticleMetaData]] = Task {
    syndEntry.map(entry =>
      ArticleMetaData(
        entry.getTitle,
        entry.getUri,
        entry.getPublishedDate,
        name,
        entry.getDescription.getValue
      ))
  }


  def filterByOldId(articles: Seq[ArticleMetaData], seqId: Seq[String]) = Task {
    articles.filter(!_.url.contains(seqId))
  }

  // Task = IO.effect
  def filterByOldIdIO(articles: Seq[ArticleMetaData], seqId: Seq[String]): Task[Seq[ArticleMetaData]] = IO.effect {
    articles.filter(!_.url.contains(seqId))
  }


  /**
   *
   * @param url
   * @return
   */
  def initRssSync(url: String): Task[mutable.Buffer[SyndEntry]] = Task {
    logger.info(s"[RSS] - Init RssSync with ${url}")
    val feedUrl = new URL(url)
    val input = new SyndFeedInput
    val feed: SyndFeed = input.build(new XmlReader(feedUrl))
    asScalaBuffer(feed.getEntries)
  }

  lazy val rssUrlsReader: Task[List[String]] = readFile("list_flux.txt")

  lazy val filterIdReader: Task[List[String]] = readFile("id_file.txt")

  lazy val bufferWriter: Task[BufferedWriter] = initBufferWriter("id_file.txt")


  implicit class BlowUpTry[T](current: Try[T]) {

    def throwIfFailed: Try[T] = current match {
      case Success(value) => current
      case Failure(exception) => throw exception
    }

  }

}
