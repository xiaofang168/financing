package com.jeff.financing.internal

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object FutureConverterImplicits {

  implicit def intFuture2Map(x: Future[Int]): Future[Map[String, String]] = x map { e =>
    Map("data" -> e.toString)
  }

  implicit def booleanFuture2Map(x: Future[Boolean]): Future[Map[String, String]] = x map { e =>
    Map("data" -> e.toString)
  }

}
