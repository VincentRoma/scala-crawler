package org.newsroom.ref

import java.io.BufferedWriter

import org.newsroom.eslastic.ESIndexer.indexArticles
import org.newsroom.eslastic.{ArticleMetaDataWithId, ESIndexer}
import org.newsroom.eslastic.action.EsActionImpl
import org.newsroom.rss.RSSScrapper.{bufferWriter, filterByOldId, filterIdReader, initRssSync, logger, parse, parseRssRow, rssUrlsReader, scrap}
import org.newsroom.schema.ArticleMetaData
import org.newsroom.utils.DateUtils
import org.newsroom.utils.FileUtils.{initBufferWriter, readFile, writeFile}
import zio.{ Task, ZIO}

import scala.util.Try


// ZIO[R,E,A]   :  R => Either[E,A]
class NewApp extends App {
  def run(args: List[String]): ZIO[Any, Throwable, Seq[Task[ZIO[Any, Throwable, Boolean]]]] = {

    val readPoliticsFile = readAllNeededFiles("politics")
    val readGeneralsFile = readAllNeededFiles("general")

    for {
      rssUrls <- readPoliticsFile
      filterI <- readGeneralsFile
    } yield {
      rssUrls
    }
  }

  def readAllNeededFiles(pathRss: String): ZIO[Any, Throwable, Seq[Task[ZIO[Any, Throwable, Boolean]]]] = {
    /* init for parralellism */
    val rssUrlsReader: Task[List[String]] = readFile(pathRss)
    val filterIdReader: Task[List[String]] = readFile("id_file.txt")
    val bufferWriter: Task[BufferedWriter] = initBufferWriter("id_file.txt")

    for {
      rssUrls <- rssUrlsReader
      filterID <- filterIdReader
      idsSeq <- bufferWriter
    } yield for {
      data <- scrap(rssUrls, filterID, idsSeq)
    } yield {
      logger.info(s"index $data")
      data
    }
  }

  /**
   *
   * @param rssList
   * @return
   */
  def scrap(rssList: Seq[String],
            seqId: Seq[String],
            initWriter: BufferedWriter): Seq[Task[ZIO[Any, Throwable, Boolean]]] = {
    rssList.map(rss => {
      ZIO.fromTry(for {
        (name, flux) <- parseRssRow(rss)
      } yield for {
        data <- initRssSync(flux)
        articlesFullSeq <- parse(data, name)
        articlesFilterSeq <- filterByOldId(articlesFullSeq, seqId)
        idFuturSeq <- indexArticles(articlesFilterSeq)
        isSucces <- writeFile(initWriter, idFuturSeq)
      } yield {
        logger.info(s"index $isSucces")
        isSucces
      })
    })
  }
}




