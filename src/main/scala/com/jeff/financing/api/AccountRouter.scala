package com.jeff.financing.api

import akka.http.scaladsl.server.Directives._
import com.jeff.financing.api.ZioSupport._
import com.jeff.financing.entity.AccountJsonSupport._
import com.jeff.financing.service.ZAccount

object AccountRouter {

  val route =
    path("accounts" / Remaining) { id =>
      val value = ZAccount
        .get(id) // the specification of the action
        .provideLayer(ZAccount.live) // plugging in a real layer/implementation to run on
      complete(value)
    }

}
