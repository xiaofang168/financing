package com.jeff.financing.api

import akka.http.scaladsl.server.Directives._
import com.jeff.financing.dto.ZioSupport._
import com.jeff.financing.entity.AccountJsonSupport._
import com.jeff.financing.service.ZAccount

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object AccountRouter extends ResponseFactory {

  private final case class Foo(bar: String)

  val route =
    path("accounts" / "test") {
      val a = Future {
        Foo("test hello world")
      }
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
      import io.circe.generic.auto._
      sendResponse(a)
    } ~
      path("accounts" / Remaining) { id =>
        val value = ZAccount
          .get(id) // the specification of the action
          .provideLayer(ZAccount.live) // plugging in a real layer/implementation to run on
        complete(value)
      } ~
      path("accounts") {
        import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
        import io.circe.generic.auto._
        sendResponse(Foo("test"))
      }

}

