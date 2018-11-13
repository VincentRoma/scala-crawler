package org.newsroom

import com.ibm.couchdb._
import java.net.URL
import com.rometools.rome.feed.synd.{SyndFeed}
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import scala.collection.JavaConversions._


object Crawler extends App{
    val couch = CouchDb("127.0.0.1", 5984)
    val db = couch.db("awesome-database", TypeMapping.empty)
    val feedUrl = new URL("https://www.lemonde.fr/politique/rss_full.xml")
    val input = new SyndFeedInput
    val feed: SyndFeed = input.build(new XmlReader(feedUrl))
    //println(feed)


    // `feed.getEntries` has type `java.util.List[SyndEntry]`
    val entries = asScalaBuffer(feed.getEntries).toVector

    for (entry <- entries) {
        println("Title: " + entry.getTitle)
        println("URI:   " + entry.getUri)
        println("Date:  " + entry.getPublishedDate)
        println("Author:  " + entry.getAuthor)
        println("Summary:  " + entry.getDescription.getValue)

        println("")
    }
}


case class Article(title: String, url: String, date: )
