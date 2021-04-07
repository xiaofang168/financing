package com.jeff.financing.internal

import zio.Task

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ResConverterImplicits {

  implicit def intFuture2Map(x: Future[Int]): Future[Map[String, String]] = x map { e =>
    Map("data" -> e.toString)
  }

  implicit def booleanFuture2Map(x: Future[Boolean]): Future[Map[String, String]] = x map { e =>
    Map("data" -> e.toString)
  }

  implicit def intTask2Map(x: Task[Int]): Task[Map[String, String]] = x map { e =>
    Map("data" -> e.toString)
  }

  implicit def booleanTask2Map(x: Task[Boolean]): Task[Map[String, String]] = for (e <- x) yield Map("data" -> e.toString)

}
