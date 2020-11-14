package com.jeff.financing.api

import akka.http.scaladsl.server.Directives.{as, complete, entity, get, onComplete, path, post, _}
import com.jeff.financing.dto.CreateStocktakingCommand
import com.jeff.financing.dto.StocktakingItemJsonSupport._
import com.jeff.financing.service.StocktakingService

import scala.util.{Failure, Success}

object StocktakingRouter {
  val stocktakingService = new StocktakingService {}
  val route =
    path("stocktaking") {
      get {
        complete(stocktakingService.find())
      } ~
        get {
          parameters("target_id") { targetId =>
            complete(stocktakingService.find(targetId))
          }
        } ~
        post {
          import com.jeff.financing.dto.CreateStocktakingJsonSupport._
          entity(as[CreateStocktakingCommand]) { createStocktakingCommand =>
            onComplete(stocktakingService.save(createStocktakingCommand)) {
              case Success(value) => complete(Map("data" -> value))
              case Failure(ex) => complete(s"资产盘点出错: ${ex.getMessage}")
            }
          }
        }
    } ~ path("stocktaking" / Remaining) { id =>
      complete(stocktakingService.getById(id))
    }

}
