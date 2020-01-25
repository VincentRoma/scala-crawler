package org.newsroom.utils

import java.nio.charset.StandardCharsets

import zio.Task

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

object HashUtils {


  def encodingToUtf8(value: String): String = new String(value.getBytes(StandardCharsets.UTF_8))

  def md5HashTryT(text: String): Task[String] = Task {
    java.security.MessageDigest.getInstance("MD5").digest(text.getBytes()).map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") {
      _ + _
    }
  }

  def md5HashTry(text: String): Try[String] = Try {
    java.security.MessageDigest.getInstance("MD5").digest(text.getBytes()).map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") {
      _ + _
    }
  }

  def md5HashFuture(text: String): Future[String] = Future {
    java.security.MessageDigest.getInstance("MD5").digest(text.getBytes()).map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") {
      _ + _
    }
  }

  def md5HashFutureT(text: String): Task[String] = Task {
    java.security.MessageDigest.getInstance("MD5").digest(text.getBytes()).map(0xFF & _).map {
      "%02x".format(_)
    }.foldLeft("") {
      _ + _
    }
  }
}
