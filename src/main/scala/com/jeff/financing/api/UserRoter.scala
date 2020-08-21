package com.jeff.financing.api

import akka.http.scaladsl.server.Directives._
import com.jeff.financing.entity.UserJsonSupport._
import com.jeff.financing.service.UserService

import scala.util.{Failure, Success}

object UserRoter extends UserService {

  val route =
    path("users" / Remaining) { id =>
      onComplete(get(id)) {
        case Success(value) => complete(value)
        case Failure(ex) => complete(s"An error occurred: ${ex.getMessage}")
      }
    }

}
