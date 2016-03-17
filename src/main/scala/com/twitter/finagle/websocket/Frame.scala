package com.twitter.finagle.websocket

import com.twitter.io.Buf

sealed trait Frame
case class Text(text: String) extends Frame
case class Binary(buf: Buf) extends Frame
case class Ping(buf: Buf) extends Frame
case class Pong(buf: Buf) extends Frame
