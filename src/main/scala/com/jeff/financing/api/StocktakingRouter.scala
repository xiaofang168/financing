package com.jeff.financing.api

import akka.http.scaladsl.server.Directives._
import com.jeff.financing.dto.ZioSupport._
import com.jeff.financing.dto.{CreateStocktakingCommand, StocktakingItem}
import com.jeff.financing.service.ZStocktaking
import zio.Task

object StocktakingRouter {

  val route =
    path("stocktaking") {
      get {
        parameters("target_id".optional) { targetId: Option[String] =>
          import com.jeff.financing.dto.StocktakingItemJsonSupport._
          if (targetId.isEmpty) {
            val result: Task[Vector[StocktakingItem]] = ZStocktaking.find()
                                                                    .provideLayer(ZStocktaking.live)
            complete(result)
          } else {
            val result: Task[Vector[StocktakingItem]] = ZStocktaking.find(targetId.get)
                                                                    .provideLayer(ZStocktaking.live)
            complete(result)
          }
        }
      } ~
        post {
          import com.jeff.financing.dto.CreateStocktakingJsonSupport._
          entity(as[CreateStocktakingCommand]) { command =>
            import com.jeff.financing.dto.ZioSupport.JsonResultSupport._
            val result: Task[Boolean] = ZStocktaking.save(command)
                                                    .provideLayer(ZStocktaking.live)
            complete(result.toJson)
          }
        }
    } ~ path("stocktaking" / Remaining) { id =>
      delete {
        import com.jeff.financing.dto.ZioSupport.JsonResultSupport._
        val result: Task[Int] = ZStocktaking.delById(id)
                                            .provideLayer(ZStocktaking.live)
        complete(result.toJson)
      } ~
        put {
          import com.jeff.financing.dto.CreateStocktakingJsonSupport._
          entity(as[CreateStocktakingCommand]) { command =>
            import com.jeff.financing.dto.ZioSupport.JsonResultSupport._
            val result: Task[Int] = ZStocktaking.update(id, command)
                                                .provideLayer(ZStocktaking.live)
            complete(result.toJson)
          }
        }
    }

}
