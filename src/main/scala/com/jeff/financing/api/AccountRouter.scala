package com.jeff.financing.api

import akka.http.scaladsl.server.Directives._
import com.jeff.financing.dto.AccountItem
import com.jeff.financing.service.ZAccount
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import zio.Task

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AccountRouter extends ResponseFactory {

  private final case class Foo(bar: String)

  val route =
    path("accounts" / "test") {
      val a = Future {
        Foo("test hello world")
      }
      sendResponse(a)
    } ~
      path("accounts" / Remaining) { id =>
        val value: Task[AccountItem] = ZAccount
          .get(id) // the specification of the action
          .provideLayer(ZAccount.live) // plugging in a real layer/implementation to run on
        sendResponse(value)
      } ~
      path("accounts") {
        sendResponse(Some(Foo("test")))
      }

}

