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
          entity(as[CreateStocktakingCommand]) { createStocktakingCommand =>
            onComplete(stocktakingService.save(createStocktakingCommand)) {
              case Success(value) => complete(Map("data" -> value))
              case Failure(ex) => complete(s"资产盘点出错: ${ex.getMessage}")
            }
          }
        }
    } ~ path("stocktaking" / Remaining) { id =>
      delete {
        onComplete(stocktakingService.delById(id)) {
          case Success(value) => complete(Map("data" -> value))
          case Failure(ex) => complete(s"删除盘点出错: ${ex.getMessage}")
        }
      } ~
        put {
          import com.jeff.financing.dto.CreateStocktakingJsonSupport._
          entity(as[CreateStocktakingCommand]) { command =>
            onComplete(stocktakingService.update(id, command)) {
              case Success(value) => complete(Map("data" -> value))
              case Failure(ex) => complete(s"修改盘点出错: ${ex.getMessage}")
            }
          }
        }
    }

}
