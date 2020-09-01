package com.jeff.financing.api

import akka.http.scaladsl.server.Directives._
import com.jeff.financing.entity.AccountJsonSupport._
import com.jeff.financing.service.AccountService

import scala.util.{Failure, Success}

object AccountRouter extends AccountService {

  val route =
    path("accounts" / Remaining) { id =>
      onComplete(get(id)) {
        case Success(value) => complete(value)
        case Failure(ex) => complete(s"An error occurred: ${ex.getMessage}")
      }
    }

}
