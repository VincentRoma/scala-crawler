package org.newsroom.utils

import java.io.{BufferedWriter, File, FileNotFoundException, FileWriter, Writer}

import org.newsroom.logger.LogsHelper
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder
import java.nio.charset.CodingErrorAction

import org.newsroom.eslastic.ArticleId

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


  def writeFile(bw: BufferedWriter, id: ArticleId): Try[Unit] = Try {
    println("greg c à ",id)
    println("eee c à ",bw) // Todo wtf
    bw.append(id + System.getProperty("line.separator"))

    val file = new File("id_file.txt")
    val bw2 = new BufferedWriter(new FileWriter(file,true))
    bw2.append(id + System.getProperty("line.separator"))
    bw2.close()

  }



  def initBufferWriter(fileName: String) = {
    Try {
      val bf  = new BufferedWriter(new FileWriter(new File(fileName), true))
      bf
    }
  }
}
