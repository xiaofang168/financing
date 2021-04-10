/*
 * Copyright 2016 Vitor Vieira
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jeff.financing.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import com.jeff.financing.dto.JsonResult

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

trait ResponseFactory {

  final case class Result[T](data: T)

  def sendResponse[T](eventualResult: Future[T])(implicit marshaller: Result[T] ⇒ ToResponseMarshallable): Route = {
    val f: Future[Result[T]] = for (r <- eventualResult) yield Result(r)
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

}
