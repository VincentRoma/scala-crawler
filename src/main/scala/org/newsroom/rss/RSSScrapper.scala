package org.newsroom.rss

import java.io
import java.io.BufferedWriter
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Date

import com.rometools.rome.feed.synd.{SyndEntry, SyndFeed}
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import org.newsroom.eslastic.ESIndexer._
import org.newsroom.logger.LogsHelper
import org.newsroom.schema.ArticleMetaData
import org.newsroom.utils.DateUtils
import org.newsroom.utils.FileUtils._
import zio.ZIO

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object RSSScrapper extends App with LogsHelper {

  run.throwIfFailed

  /**
   *
   */

  def run: Try[Seq[Try[Try[Seq[ZIO[Any, Throwable, Future[Try[Unit]]]]]]]] = {
    for {
      rssUrls <- rssUrlsReader
      filterI <- filterIdReader
      idWriter <- bufferWriter // Uselees je veux écrire en stream
    } yield {
      scrap(rssUrls, filterI, idWriter)
    }
  }


  // : Seq[Try[Try[Seq[ZIO[Any, Throwable, Future[Try[Unit]]]]]]]
  /**
   *
   * @param rssList
   * @return
   */
  def scrap(rssList: Seq[String],
            seqId: Seq[String],
            initWriter: BufferedWriter): Seq[Try[Try[Seq[ZIO[Any, Throwable, Future[Try[Unit]]]]]]] = {
    rssList.map(rss => {
      for {
        (name, flux) <- parseRssRow(rss)
      } yield for {
        data <- initRssSync(flux)
        articlesFullSeq <- parse(data, name)
        articlesFilterSeq <- filterByOldId(articlesFullSeq, seqId)
      } yield for {
        article <- articlesFilterSeq
      } yield for {
        idFuturSeq <- indexArticle(article)
      } yield for {
        id <- idFuturSeq
      } yield for {
        isSucces <- writeFile(initWriter, id).throwIfFailed
      } yield {
        logger.info(s"- [RSS] - Success indexing : $name ")
        isSucces
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


  def parse(syndEntry: mutable.Buffer[SyndEntry], name: String): Try[mutable.Buffer[ArticleMetaData]] = Try {
    syndEntry.map(entry =>
      ArticleMetaData(
        entry.getTitle,
        entry.getUri,
        entry.getPublishedDate,
        name,
        entry.getDescription.getValue
      ))
  }


  def filterByOldId(articles: Seq[ArticleMetaData], seqId: Seq[String]) = Try {
    articles.filter(!_.url.contains(seqId))
  }


  /**
   *
   * @param url
   * @return
   */
  def initRssSync(url: String): Try[mutable.Buffer[SyndEntry]] = Try {
    logger.info(s"[RSS] - Init RssSync with ${url}")
    val feedUrl = new URL(url)
    val input = new SyndFeedInput
    val feed: SyndFeed = input.build(new XmlReader(feedUrl))
    asScalaBuffer(feed.getEntries)
  }

  lazy val rssUrlsReader: Try[List[String]] = readFile("list_flux.txt")

  lazy val filterIdReader: Try[List[String]] = readFile("id_file.txt") recoverWith {
    case e: Exception => Failure(new Exception("Need ce putain de fichier", e))
  }

  lazy val bufferWriter: Try[BufferedWriter] = initBufferWriter("id_file.txt") recoverWith {
    case e: Exception => Failure(new Exception("Need ce zzz de fichier", e))
  }


  implicit class BlowUpTry[T](current: Try[T]) {

    def throwIfFailed: Try[T] = current match {
      case Success(value) => current
      case Failure(exception) => throw exception
    }

  }

}
