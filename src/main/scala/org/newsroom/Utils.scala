package org.newsroom

import java.io.{BufferedWriter, File, FileWriter}
import java.util.Date
import java.text.SimpleDateFormat

import scala.util.Try

/**
 *
 */
object Utils {

  /**
   *
   * @param filename
   * @return
   */
  def readFile(filename: String): Try[List[String]] = {
    Try {
      val bufferedSource = io.Source.fromFile(filename)
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
    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))
    for (line <- lines) {
      bw.append(line + System.getProperty("line.separator"))
    }
    bw.close()
  }

  val DATE_FORMAT = "EEE, MMM dd, yyyy h:mm a"

  /**
   *
   * @param d
   * @return
   */
  def getDateAsString(d: Date): String = {
    val dateFormat = new SimpleDateFormat(DATE_FORMAT)
    dateFormat.format(d)
  }

  /**
   *
   * @param s
   * @return
   */
  def convertStringToDate(s: String): Date = {
    val dateFormat = new SimpleDateFormat(DATE_FORMAT)
    dateFormat.parse(s)
  }
}
