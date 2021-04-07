package com.jeff.financing.api

import akka.http.scaladsl.server.Directives._
import com.jeff.financing.dto.FlowItemJsonSupport._
import com.jeff.financing.dto.ZioSupport._
import com.jeff.financing.dto.{CreateFlowCommand, FlowItem}
import com.jeff.financing.service.ZFlow
import zio._

import scala.language.postfixOps

object FlowRouter {

  val route =
    path("flows") {
      get {
        parameters("start_date".optional, "end_date".optional, "platform".optional, "category".optional) { (startDate: Option[String], endDate: Option[String], platform: Option[String], category: Option[String]) =>
          val result = ZFlow
            .list(startDate, endDate, platform, category)
            .provideLayer(ZFlow.live)
          complete(result)
        }
      } ~
        post {
          import com.jeff.financing.dto.CreateFlowCommandJsonSupport._
          entity(as[CreateFlowCommand]) { command =>
            import com.jeff.financing.dto.ZioSupport.JsonResultSupport._
            val result = ZFlow.save(command)
                              .provideLayer(ZFlow.live)
            complete(result.toJson)
          }
        }
    } ~ path("flows" / Remaining) { id =>
      get {
        val result: Task[FlowItem] = ZFlow.get(id)
                                          .provideLayer(ZFlow.live)
        complete(result)
      } ~
        delete {
          import com.jeff.financing.dto.ZioSupport.JsonResultSupport._
          val result: Task[Int] = ZFlow.delById(id)
                                       .provideLayer(ZFlow.live)
          complete(result.toJson)
        } ~
        put {
          import com.jeff.financing.dto.CreateFlowCommandJsonSupport._
          entity(as[CreateFlowCommand]) { command =>
            import com.jeff.financing.dto.ZioSupport.JsonResultSupport._
            val result: Task[Int] = ZFlow.update(id, command)
                                         .provideLayer(ZFlow.live)
            complete(result.toJson)
          }
        }
    }

}
