package org.newsroom

import java.net.URL
import java.util.Date

import com.rometools.rome.feed.synd.{SyndEntry, SyndFeed}
import com.rometools.rome.io.{SyndFeedInput, XmlReader}

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable
import scala.util.{Failure, Success}

object RSSScrapper extends App {


  run

  def run = {
    rssUrlsReader match {
      case Success(ressUrlsList) =>
        println("start scrap")
        val metaArticlesSeqFilter: Seq[Seq[ArticleMetaData]] = filterIdReader match {
          case Success(value) =>
            println("reprise de la collecte")
            scrap(ressUrlsList)
            .map(seqSource => seqSource.filter(entry => !value.contains(entry.url)))
          case Failure(_) =>
            println("premier run")
            scrap(ressUrlsList)
        }

        println("ESIndexer start")
        ESIndexer(metaArticlesSeqFilter.flatten).run
        Utils.writeFile("id_file.txt",metaArticlesSeqFilter.flatten.map(_.url))
      case Failure(_) =>
        println("le fichier fdp")
        System.exit(0)
    }
  }

  def scrap(rssList: Seq[String]): Seq[Seq[ArticleMetaData]] = {
    rssList.map(rss => {
      println(s"Start collecting ${rss.split("¤")(0)}")
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
    val feedUrl = new URL(url)
    val input = new SyndFeedInput
    val feed: SyndFeed = input.build(new XmlReader(feedUrl))
    asScalaBuffer(feed.getEntries)
  }

  lazy val rssUrlsReader = Utils.readFile("list_flux.txt")

  lazy val filterIdReader = Utils.readFile("id_file.txt")

  case class ArticleMetaData(title: String, url: String, publishedDate: Date, author: String, description: String)

}
