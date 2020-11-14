package com.jeff.financing.service

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait DataConverter[T, R] {

  def convert2Vector(future: Future[Vector[T]], fn: T => R): Future[Vector[R]] = {
    for {
      result <- future
    } yield {
      result.map(fn(_))
    }
  }

  def convert2Obj(future: Future[Option[T]], fn: T => R): Future[Option[R]] = {
    for {
      result <- future
    } yield {
      result.map(fn(_))
    }
  }

}
