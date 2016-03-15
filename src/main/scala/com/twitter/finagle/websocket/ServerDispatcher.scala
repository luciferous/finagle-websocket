package com.twitter.finagle.websocket

import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.Service
import com.twitter.finagle.netty3.{BufChannelBuffer, ChannelBufferBuf}
import com.twitter.finagle.stats.StatsReceiver
import com.twitter.finagle.transport.Transport
import com.twitter.io.Buf
import com.twitter.util.{Closable, Future, Time}
import org.jboss.netty.handler.codec.http.websocketx._

private[finagle] class ServerDispatcher(
    trans: Transport[Any, Any],
    service: Service[Request, Response],
    stats: StatsReceiver)
  extends Closable {

  import ServerDispatcher._

  private[this] def messages(): AsyncStream[Any] =
    AsyncStream.fromFuture(trans.read()) ++ messages()

  private[this] val request = Request(messages.map(fromNetty))

  service(request).flatMap { response =>
    response.messages
      .map(toNetty)
      .foreachF(trans.write)
      .ensure(trans.close())
  }

  def close(deadline: Time): Future[Unit] =
    trans.close()
}

private object ServerDispatcher {
  def fromNetty(m: Any): Frame = m match {
    case text: TextWebSocketFrame =>
      Text(text.getText)

    case cont: ContinuationWebSocketFrame =>
      Text(cont.getText)

    case bin: BinaryWebSocketFrame =>
      Binary(new ChannelBufferBuf(bin.getBinaryData))

    // TODO don't do this
    case _ => throw new Exception("unknown frame")
  }

  def toNetty(frame: Frame): WebSocketFrame = frame match {
    case Text(message) =>
      new TextWebSocketFrame(message)

    case Binary(buf) =>
      new BinaryWebSocketFrame(BufChannelBuffer(buf))
  }
}
