package com.jeff.financing.api

import akka.http.scaladsl.server.Directives._
import com.jeff.financing.entity.AccountJsonSupport._
import com.jeff.financing.service.AccountService

object AccountRouter extends AccountService {

  val route =
    path("accounts" / Remaining) { id =>
      complete(get(id))
    }

}
