package com.twitter.finagle.websocket

import com.twitter.concurrent.AsyncStream
import java.net.URI
import scala.collection.immutable

case class Request(
    uri: URI,
    headers: immutable.Map[String, String],
    messages: AsyncStream[Frame])
