package com.jeff.financing.api

import akka.http.scaladsl.marshalling.{Marshaller, Marshalling}
import akka.http.scaladsl.model.HttpResponse
import zio.{IO, Runtime}

import scala.concurrent.Promise
import scala.language.implicitConversions


object ZioSupport {

  implicit def zioMarshaller[A, E](implicit m1: Marshaller[A, HttpResponse], m2: Marshaller[E, HttpResponse]): Marshaller[IO[E, A], HttpResponse] =
    Marshaller { implicit ec =>
      a => {
        val r = a.foldM(
          err => IO.fromFuture(implicit ec => m2(err)),
          suc => IO.fromFuture(implicit ec => m1(suc))
        )

        val p = Promise[List[Marshalling[HttpResponse]]]()

        Runtime.default.unsafeRunAsync(r) { exit =>
          exit.fold(
            failed => p.failure(failed.squash),
            success => p.success(success)
          )
        }

        p.future
      }
    }

}