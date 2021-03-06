package org.newsroom

import java.net.URL
import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.{SyndFeedInput, XmlReader}


object Crawler extends App{
    val feedUrl = new URL("https://www.lemonde.fr/politique/rss_full.xml")
    val input = new SyndFeedInput
    val feed: SyndFeed = input.build(new XmlReader(feedUrl))
}
