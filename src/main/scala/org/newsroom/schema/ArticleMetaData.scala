package org.newsroom.schema

import java.util.Date

case class ArticleMetaData(title: String, url: String, publishedDate: Date, author: String, description: String)
