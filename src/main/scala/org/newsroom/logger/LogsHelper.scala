package org.newsroom.logger

import com.typesafe.scalalogging._

trait LogsHelper {
  val logger: Logger = Logger("RSS_LOGS")
}
