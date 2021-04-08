package com.jeff.financing.api

import akka.http.scaladsl.server.Directives._
import com.jeff.financing.dto.ZioSupport._
import com.jeff.financing.entity.AccountJsonSupport._
import com.jeff.financing.service.ZAccount

object AccountRouter {

  private final case class Foo(bar: String)

  private final case class Result[T](data: T)

  val route =
    path("accounts" / Remaining) { id =>
      val value = ZAccount
        .get(id) // the specification of the action
        .provideLayer(ZAccount.live) // plugging in a real layer/implementation to run on
      complete(value)
    } ~
      path("accounts") {
        import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
        import io.circe.generic.auto._
        complete(Result(Foo("test")))
      }

}
