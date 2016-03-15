package com.twitter.finagle.websocket

import com.twitter.concurrent.AsyncStream

case class Request(messages: AsyncStream[Frame])
