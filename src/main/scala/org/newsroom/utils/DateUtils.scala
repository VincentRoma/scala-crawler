package org.newsroom.utils

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.{Calendar, Date, TimeZone}

object DateUtils {
  val DATE_FORMAT = "EEE, MMM dd, yyyy h:mm a"

  /**
   *
   * @param d
   * @return
   */
  def getDateAsString(d: Date, format: Option[String]): String = {

    new SimpleDateFormat(format match {
      case Some(format) => format
      case None => DATE_FORMAT
    })
      .format(d)
  }

  /**
   *
   * @param s
   * @return
   */
  def convertStringToDate(s: String, formatT: Option[String]): Date = {
    new SimpleDateFormat(formatT match {
      case Some(value) => value
      case None => DATE_FORMAT
    }).parse(s)
  }


  def getTime(minusHour:Int)= {
    val format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    val cal = Calendar.getInstance
    cal.add(Calendar.HOUR,minusHour)
    format.format(cal.getTime)
  }



}
