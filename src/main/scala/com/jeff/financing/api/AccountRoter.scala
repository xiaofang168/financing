package com.jeff.financing.api

import akka.http.scaladsl.server.Directives._
import com.jeff.financing.entity.AccountJsonSupport._
import com.jeff.financing.service.UserService

import scala.util.{Failure, Success}

object AccountRoter extends UserService {

  val route =
    path("accounts" / Remaining) { id =>
      onComplete(get(id)) {
        case Success(value) => complete(value)
        case Failure(ex) => complete(s"An error occurred: ${ex.getMessage}")
      }
    }

}
