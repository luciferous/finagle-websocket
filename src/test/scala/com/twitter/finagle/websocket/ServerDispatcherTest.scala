package com.twitter.finagle.websocket

import com.twitter.concurrent.AsyncQueue
import com.twitter.conversions.time._
import com.twitter.finagle.{Service, Status}
import com.twitter.finagle.stats.DefaultStatsReceiver
import com.twitter.finagle.transport.{QueueTransport, Transport}
import com.twitter.util.{Await, Future}
import org.junit.runner.RunWith
import org.jboss.netty.handler.codec.http._
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ServerDispatcherTest extends FunSuite {
  import ServerDispatcherTest._

  val echo = Service.mk { req: Request => Future.value(Response(req.messages)) }

  test("invalid message") {
    val (in, out) = mkPair[Any, Any]
    val disp = new ServerDispatcher(out, echo, DefaultStatsReceiver)
    in.write("invalid")
    Await.ready(out.onClose)
    assert(out.status == Status.Closed)
  }
}

object ServerDispatcherTest {
  def mkPair[A,B] = {
    val inq = new AsyncQueue[A]
    val outq = new AsyncQueue[B]
    (new QueueTransport[A, B](inq, outq), new QueueTransport[B, A](outq, inq))
  }
}
