package com.jeff.financing.api


import akka.http.scaladsl.server.Directives._
import com.jeff.financing.dto.CreateFlowCommand
import com.jeff.financing.dto.FlowItemJsonSupport._
import com.jeff.financing.service.FlowService

import scala.util.{Failure, Success}

object FlowRouter {
  val flowService = new FlowService {}

  val route =
    path("flows") {
      get {
        complete(flowService.list())
      } ~
        post {
          import com.jeff.financing.dto.CreateFlowCommandJsonSupport._
          entity(as[CreateFlowCommand]) { command =>
            onComplete(flowService.save(command)) {
              case Success(value) => complete(Map("data" -> value))
              case Failure(ex) => complete(s"An error occurred: ${ex.getMessage}")
            } gt
          }
        }
    } ~ path("flows" / Remaining) { id =>
      complete(flowService.get(id))
    }

}
