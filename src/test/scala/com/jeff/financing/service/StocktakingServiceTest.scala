package com.jeff.financing.service

import com.jeff.financing.dto.CreateStocktakingCommand
import org.junit.Test

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.util.{Failure, Success}

class StocktakingServiceTest {

  @Test
  def save(): Unit = {
    val stocktakingService = new StocktakingService {}
    val f = stocktakingService.save(CreateStocktakingCommand("5fa3bddb12a8f0d0e622b32a", "2020-11-01", 50000, Some("定投了1500")))
    f onComplete {
      case Success(value) => println(value)
      case Failure(exception) => exception.printStackTrace()
    }
    Thread.sleep(5000)
  }

}
