package com.jeff.financing.api

import akka.http.scaladsl.server.Directives._
import com.jeff.financing.dto.CreateStocktakingCommand
import com.jeff.financing.dto.StocktakingItemJsonSupport._
import com.jeff.financing.dto.ZioSupport._
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
            import com.jeff.financing.dto.ZioSupport.JsonResultSupport._
            val result = stocktakingService.save(command)
            complete(result.toJson)
          }
        }
    } ~ path("stocktaking" / Remaining) { id =>
      delete {
        import com.jeff.financing.dto.ZioSupport.JsonResultSupport._
        val result: Task[Int] = stocktakingService.delById(id)
        complete(result.toJson)
      } ~
        put {
          import com.jeff.financing.dto.CreateStocktakingJsonSupport._
          entity(as[CreateStocktakingCommand]) { command =>
            import com.jeff.financing.dto.ZioSupport.JsonResultSupport._
            val result: Task[Int] = stocktakingService.update(id, command)
            complete(result.toJson)
          }
        }
    }

}
