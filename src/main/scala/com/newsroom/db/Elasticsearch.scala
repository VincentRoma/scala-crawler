package com.newsroom.db

import scalaj.http.{Http, HttpOptions}


class Elasticsearch {
  val cluster = "http://127.0.0.1:9200/"
  def insertDocument(article: String, index:String): Unit = {
    val result = Http(cluster+index+"/_doc/").postData(article)
      .header("Content-Type", "application/json")
      .header("Charset", "UTF-8")
      .option(HttpOptions.readTimeout(10000)).asString
  }
}
