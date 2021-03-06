package org.newsroom.utils

import java.io.{BufferedWriter, File, FileWriter}

import org.newsroom.logger.LogsHelper
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder
import java.nio.charset.CodingErrorAction

import scala.util.Try

/**
 *
 */
object FileUtils extends LogsHelper {

  /**
   *
   * @param filename
   * @return
   */
  def readFile(filename: String): Try[List[String]] = {
    logger.info(s"[RSS] - Reading File ${filename}")
    Try {
      val decoder = Charset.forName("UTF-8").newDecoder
      decoder.onMalformedInput(CodingErrorAction.IGNORE)
      val bufferedSource = io.Source.fromFile(filename)(decoder)
      val lines = (for (line <- bufferedSource.getLines()) yield line).toList
      bufferedSource.close
      lines
    }
  }

  /**
   *
   * @param filename
   * @param lines
   */
  def writeFile(filename: String, lines: Seq[String]): Unit = {
    logger.info(s"[RSS] - Update File ${filename}")
    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file,true))
    for (line <- lines) {
      bw.append(line + System.getProperty("line.separator"))
    }
    bw.close()
  }
}
