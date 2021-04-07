package com.jeff.financing.api

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import com.jeff.financing.dto.ZioSupport._
import zio.{Task, ZIO}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object IndexRouter {

  def divide(a: Int, b: Int): Future[Int] = Future {
    a / b
  }

  val route =
    path("index") {
      get {
        val a = HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>")
        val value: Task[HttpEntity.Strict] = ZIO(a)
        complete(value)
      }
    } ~
      path("divide" / IntNumber / IntNumber) { (a, b) =>
        onComplete(divide(a, b)) {
          case Success(value) => complete(s"The result was $value")
          case Failure(ex) => complete(s"An error occurred: ${ex.getMessage}")
        }
      }

}