package org.newsroom.rss

import java.net.URL
import java.util.Date

import com.rometools.rome.feed.synd.{SyndEntry, SyndFeed}
import com.rometools.rome.io.{SyndFeedInput, XmlReader}
import org.newsroom.eslastic.ESIndexer
import org.newsroom.logger.LogsHelper
import org.newsroom.utils.{DateUtils, FileUtils}

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable
import scala.util.{Failure, Success, Try}

object RSSScrapper extends App with LogsHelper {

  run

  /**
   *
   */
  def run = {
    rssUrlsReader match {
      case Success(ressUrlsList) =>
        logger.info("[RSS] - Start Scrapping RSS flux")
        val metaArticlesSeqFilter: Seq[Seq[ArticleMetaData]] = filterIdReader match {
          case Success(value) =>
            val a = scrap(ressUrlsList)
            logger.info("[RSS] - Start Scrapping RSS flux without old urls")
            scrap(ressUrlsList)
              .map(seqSource => seqSource
                .filter(_.title.equals("fail"))
                .filter(entry => !value.contains(entry.url)))
          case Failure(_) =>
            logger.info("[RSS] - Start Scrapping RSS all flux")
            scrap(ressUrlsList)
        }
        logger.info("[RSS] - ES Indexing start")
//        ESIndexer(metaArticlesSeqFilter.flatten).run
        logger.info("[RSS] - ES Indexing end")


        FileUtils.writeFile("id_file.txt", metaArticlesSeqFilter.flatten.map(_.url))


        logger.info("[RSS] - RSSScrapper end")
        logger.info("                      ")
        logger.info("                      ")
        logger.info("                      ")

      case Failure(e: Exception) =>
        logger.error("[RSS] - " + e.getMessage)
        System.exit(0)
    }
  }


  /**
   *
   * @param row
   * @return
   */
  def parseRssRow(row: String): (String, String) = {
    val name = row.split("\";\"")(0)
    val flux = row.split("\";\"")(1)
    (name.substring(1), flux)
  }


  /**
   *
   * @param rssList
   * @return
   */
  def scrap(rssList: Seq[String]): Seq[Seq[ArticleMetaData]] = {
    rssList.map(rss => {
      Try {
        val (name, flux) = parseRssRow(rss)
        logger.info(s"[RSS] - Start collect ${name}")
        val rssArticles: Seq[ArticleMetaData] = initRssSync(flux).map(entry => {
          ArticleMetaData(
            entry.getTitle,
            entry.getUri,
            entry.getPublishedDate,
            name,
            entry.getDescription.getValue
          )
        })



        /* Run Indexer */
        ESIndexer(rssArticles).run


        /* Return file_id to filter */
        rssArticles
      } match {
        case Success(value) => value
        case Failure(ex) =>
          logger.error(s"[RSS] - Error collect for one RSS")
          Seq(ArticleMetaData("fail", "", DateUtils.convertStringToDate("20190101",Some("yyyyMMdd")), "", ""))
      }
    })
  }

  /**
   *
   * @param url
   * @return
   */
  def initRssSync(url: String): mutable.Buffer[SyndEntry] = {
    logger.info(s"[RSS] - Init RssSync with ${url}")
    val feedUrl = new URL(url)
    val input = new SyndFeedInput
    val feed: SyndFeed = input.build(new XmlReader(feedUrl))
    asScalaBuffer(feed.getEntries)
  }

  lazy val rssUrlsReader = FileUtils.readFile("list_flux.txt")

  lazy val filterIdReader = FileUtils.readFile("id_file.txt")

  case class ArticleMetaData(title: String, url: String, publishedDate: Date, author: String, description: String)

}
