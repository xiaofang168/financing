package com.jeff.financing.dto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.{Marshaller, Marshalling}
import akka.http.scaladsl.model.HttpResponse
import com.jeff.financing.internal.LowerCaseWithUnderscores
import zio.{BootstrapRuntime, IO, Task}

import scala.concurrent.Promise

case class JsonResult(data: String)

object ZioSupport extends BootstrapRuntime {

  object JsonResultSupport extends LowerCaseWithUnderscores with SprayJsonSupport {
    implicit val jsonResultFormats = jsonFormat1(JsonResult)
  }

  implicit class intTask2Map(x: Task[Int]) {
    //增加括号
    def toJson: Task[JsonResult] = x map { e =>
      JsonResult(e.toString)
    }
  }

  implicit class booleanTask2Map(x: Task[Boolean]) {
    //增加括号
    def toJson: Task[JsonResult] = for (e <- x) yield JsonResult(e.toString)
  }

  implicit def zioMarshaller[A, E](implicit m1: Marshaller[A, HttpResponse], m2: Marshaller[E, HttpResponse]): Marshaller[IO[E, A], HttpResponse] =
    Marshaller { implicit ec =>
      effect => {

        val marshalledEffect: IO[Throwable, List[Marshalling[HttpResponse]]] = effect.foldM(
          err => IO.fromFuture(implicit ec => m2(err)),
          suc => IO.fromFuture(implicit ec => m1(suc))
        )

        val p = Promise[List[Marshalling[HttpResponse]]]()

        unsafeRunAsync(marshalledEffect) { exit =>
          exit.fold(
            failed => p.failure(failed.squash),
            success => p.success(success)
          )
        }

        p.future
      }
    }


}
