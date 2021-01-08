package com.jeff.financing.service

import org.junit.Test

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.util.{Failure, Success}

class MonthlyReportServiceTest {

  @Test
  def findIncome(): Unit = {
    val service = new MonthlyReportService {}
    val f = service.findIncome(202011, 202012)
    f onComplete {
      case Success(value) => println(value)
      case Failure(exception) => exception.printStackTrace()
    }
    Thread.sleep(5000)
  }

  @Test
  def findAssert(): Unit = {
    val service = new MonthlyReportService {}
    val f = service.findAssert(202011, 202012, 0)
    f onComplete {
      case Success(value) => println(value)
      case Failure(exception) => exception.printStackTrace()
    }
    Thread.sleep(5000)
  }

}
