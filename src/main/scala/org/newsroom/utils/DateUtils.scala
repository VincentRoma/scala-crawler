package org.newsroom.utils

import java.text.SimpleDateFormat
import java.util.Date

object DateUtils {
  val DATE_FORMAT = "yyyyMMdd"

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
