package com.jeff.financing.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.server.Directives.{onComplete, _}
import akka.http.scaladsl.server._
import com.jeff.financing.dto.JsonResult
import zio.{BootstrapRuntime, Task}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}

trait ResponseFactory extends BootstrapRuntime {

  final case class Result[T](data: T)

  def sendResponse[T](future: Future[T])(implicit marshaller: Result[T] ⇒ ToResponseMarshallable): Route = {
    val f: Future[Result[T]] = for (r <- future) yield Result(r)
    onComplete(f) {
      case Success(result) ⇒
        complete(result)
      case Failure(e) ⇒
        import com.jeff.financing.dto.ZioSupport.JsonResultSupport._
        complete(ToResponseMarshallable(InternalServerError → JsonResult(e.getMessage)))
    }
  }

  def sendResponse[T](data: T)(implicit marshaller: Result[T] ⇒ ToResponseMarshallable): Route = {
    val f = Future(Result(data))
    sendResponse(f) {
      implicit res => res.data
    }
  }

  def sendResponse[T](task: Task[T])(implicit marshaller: Result[T] ⇒ ToResponseMarshallable): Route = {
    val p = Promise[T]()
    unsafeRunAsync(task) { exit =>
      exit.fold(
        failed => p.failure(failed.squash),
        success => p.success(success)
      )
    }
    val f: Future[Result[T]] = for (r <- p.future) yield Result(r)
    sendResponse(f) {
      implicit res => res.data
    }
  }

}
