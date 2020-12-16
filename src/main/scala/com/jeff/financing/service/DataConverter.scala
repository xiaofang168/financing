package com.jeff.financing.service

import cats.implicits._

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

  def convert2VectorWithFuture(future: Future[Vector[T]], fn: T => Future[R]): Future[Vector[R]] = {
    val r = for {
      result <- future
    } yield {
      val a: Vector[Future[R]] = result.map(fn(_))
      val b: Future[Vector[R]] = a.traverse(identity)
      b
    }
    r.flatten
  }

  def convert2Obj(future: Future[Option[T]], fn: T => R): Future[Option[R]] = {
    for {
      result <- future
    } yield {
      result.map(fn(_))
    }
  }

  def convert2ObjWithFuture(future: Future[Option[T]], fn: T => Future[R]): Future[Option[R]] = {
    val r = for {
      result <- future
    } yield {
      val a: Option[Future[R]] = result.map(fn(_))
      a.traverse(identity)
    }
    r.flatten
  }

}
