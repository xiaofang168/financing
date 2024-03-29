package com.jeff.financing.service

import com.jeff.financing.dto.CreateStocktakingCommand
import org.junit.Test
import reactivemongo.api.bson.document

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.util.{Failure, Success}

class StocktakingServiceTst {

  @Test
  def save(): Unit = {
    val stocktakingService = new StocktakingService {}
    val f = stocktakingService.save(CreateStocktakingCommand("5fa3bddb12a8f0d0e622b32a", "2020-11-01", 50000, 20, Some(10), Some(4.5), Some("定投了1500")))
    f onComplete {
      case Success(value) => println(value)
      case Failure(exception) => exception.printStackTrace()
    }
    Thread.sleep(5000)
  }

  @Test
  def delById(): Unit = {
    val stocktakingService = new StocktakingService {}
    val f = stocktakingService.delById("5faf475f5259308b1195463d")
    f onComplete {
      case Success(value) => println(value)
      case Failure(exception) => exception.printStackTrace()
    }
    Thread.sleep(5000)
  }

  @Test
  def aggregate(): Unit = {
    import com.jeff.financing.repository.PersistenceImplicits.stocktakingStatsReader
    val stocktakingService = new StocktakingService {}
    val f = stocktakingService.aggregate(document("targetId" -> document("$in" -> List("1", "2"))))
    f onComplete {
      case Success(value) => println(value)
      case Failure(exception) => exception.printStackTrace()
    }
    Thread.sleep(5000)
  }

}
