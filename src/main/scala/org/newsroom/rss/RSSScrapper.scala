package org.newsroom.rss

import java.net.URL
import java.util.Date

import com.rometools.rome.feed.synd.{SyndEntry, SyndFeed}
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import org.newsroom.eslastic.ESIndexer
import org.newsroom.logger.LogsHelper
import org.newsroom.utils.FileUtils

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable
import scala.util.{Failure, Success}

object RSSScrapper extends App with LogsHelper{

  run

  def run = {
    rssUrlsReader match {
      case Success(ressUrlsList) =>
        logger.info("[RSS] - Start Scrapping RSS flux")
        val metaArticlesSeqFilter: Seq[Seq[ArticleMetaData]] = filterIdReader match {
          case Success(value) =>
            logger.info("[RSS] - Start Scrapping RSS flux without old urls")
            scrap(ressUrlsList)
            .map(seqSource => seqSource.filter(entry => !value.contains(entry.url)))
          case Failure(_) =>
            logger.info("[RSS] - Start Scrapping RSS all flux")
            scrap(ressUrlsList)
        }

        logger.info("[RSS] - ES Indexing start")
        ESIndexer(metaArticlesSeqFilter.flatten).run
        logger.info("[RSS] - ES Indexing end")


        FileUtils.writeFile("/apps/dev/id_file.txt",metaArticlesSeqFilter.flatten.map(_.url))


        logger.info("[RSS] - RSSScrapper end")
        logger.info("                      ")
        logger.info("                      ")
        logger.info("                      ")

      case Failure(e:Exception) =>
        logger.error("[RSS] - "+e.getMessage)
        System.exit(0)
    }
  }

  def scrap(rssList: Seq[String]): Seq[Seq[ArticleMetaData]] = {
    rssList.map(rss => {
      logger.info(s"[RSS] - Start collection ${rss.split("¤")(0)}")
      initRssSync(rss.split("¤")(1)).map(entry => {
        ArticleMetaData(
          entry.getTitle,
          entry.getUri,
          entry.getPublishedDate,
          rss.split("¤")(0),
          entry.getDescription.getValue
        )
      })
    })
  }

  def initRssSync(url: String): mutable.Buffer[SyndEntry] = {
    logger.info(s"[RSS] - Init RssSync with ${url}")
    val feedUrl = new URL(url)
    val input = new SyndFeedInput
    val feed: SyndFeed = input.build(new XmlReader(feedUrl))
    asScalaBuffer(feed.getEntries)
  }

  lazy val rssUrlsReader = FileUtils.readFile("/apps/dev/list_flux.txt")

  lazy val filterIdReader = FileUtils.readFile("/apps/dev/id_file.txt")

  case class ArticleMetaData(title: String, url: String, publishedDate: Date, author: String, description: String)

}
