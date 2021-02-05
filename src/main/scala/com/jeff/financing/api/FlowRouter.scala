package com.jeff.financing.api


import akka.http.scaladsl.server.Directives._
import com.jeff.financing.api.ZioSupport._
import com.jeff.financing.dto.CreateFlowCommand
import com.jeff.financing.dto.FlowItemJsonSupport._
import com.jeff.financing.internal.FutureConverterImplicits._
import com.jeff.financing.service.FlowService
import zio._

import scala.concurrent.Future
import scala.language.postfixOps

object FlowRouter {
  val flowService = new FlowService {}
  val route =
    path("flows") {
      get {
        parameters("start_date".optional, "end_date".optional, "platform".optional, "category".optional) { (startDate: Option[String], endDate: Option[String], platform: Option[String], category: Option[String]) =>
          complete(flowService.list(startDate, endDate, platform, category))
        }
      } ~
        post {
          import com.jeff.financing.dto.CreateFlowCommandJsonSupport._
          entity(as[CreateFlowCommand]) { command =>
            val result: Future[Map[String, String]] = flowService.save(command)
            complete(result)
          }
        }
    } ~ path("flows" / Remaining) { id =>
      get {
        complete(flowService.get(id))
      } ~
        delete {
          val result: Future[Map[String, String]] = flowService.delById(id)
          complete(result)
        } ~
        put {
          import com.jeff.financing.dto.CreateFlowCommandJsonSupport._
          entity(as[CreateFlowCommand]) { command =>
            val result = flowService.update(id, command)
            val value: ZIO[Any, Throwable, Map[String, String]] = result.map(e => Map("data" -> e.toString))
            complete(value)
          }
        }
    }

}
