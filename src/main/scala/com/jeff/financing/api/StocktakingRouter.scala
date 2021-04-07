package com.jeff.financing.api

import akka.http.scaladsl.server.Directives._
import com.jeff.financing.api.ZioSupport._
import com.jeff.financing.dto.CreateStocktakingCommand
import com.jeff.financing.dto.StocktakingItemJsonSupport._
import com.jeff.financing.internal.ResConverterImplicits._
import com.jeff.financing.service.StocktakingService
import zio.Task

object StocktakingRouter {
  val stocktakingService = new StocktakingService {}
  val route =
    path("stocktaking") {
      get {
        parameters("target_id".optional) { targetId: Option[String] =>
          if (targetId.isEmpty) {
            complete(stocktakingService.find())
          } else {
            complete(stocktakingService.find(targetId.get))
          }
        }
      } ~
        post {
          import com.jeff.financing.dto.CreateStocktakingJsonSupport._
          entity(as[CreateStocktakingCommand]) { command =>
            val result: Task[Map[String, String]] = stocktakingService.save(command)
            complete(result)
          }
        }
    } ~ path("stocktaking" / Remaining) { id =>
      delete {
        val result: Task[Map[String, String]] = stocktakingService.delById(id)
        complete(result)
      } ~
        put {
          import com.jeff.financing.dto.CreateStocktakingJsonSupport._
          entity(as[CreateStocktakingCommand]) { command =>
            val result: Task[Map[String, String]] = stocktakingService.update(id, command)
            complete(result)
          }
        }
    }

}
