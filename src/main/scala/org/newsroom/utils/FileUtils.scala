package org.newsroom.utils

import java.io.{BufferedWriter, File, FileNotFoundException, FileWriter, Writer}

import org.newsroom.logger.LogsHelper
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder
import java.nio.charset.CodingErrorAction

import org.newsroom.eslastic.ArticleId
import zio.Task

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
 *
 */
object FileUtils extends LogsHelper {

  /**
   *
   * @param filename
   * @return
   */
  def readFile(filename: String): Task[List[String]] = {
    logger.info(s"[RSS] - Reading File ${filename}")
    Task {
      val decoder = Charset.forName("UTF-8").newDecoder
      decoder.onMalformedInput(CodingErrorAction.IGNORE)
      val bufferedSource = io.Source.fromFile(filename)(decoder)
      val lines = (for (line <- bufferedSource.getLines()) yield line).toList
      bufferedSource.close
      lines
    }
  }


  def writeFile(bw: BufferedWriter, idSeq: Seq[Task[Future[ArticleId]]]): Task[Boolean] = Task {
    val file = new File("id_file.txt")
    val bw2 = new BufferedWriter(new FileWriter(file,true))
    for (id <- idSeq) {
      bw2.append(id + System.getProperty("line.separator"))
    }
    bw2.close()
    true
  }



  def initBufferWriter(fileName: String) =
    Task {
      val bf  = new BufferedWriter(new FileWriter(new File(fileName), true))
      bf
    }
}
