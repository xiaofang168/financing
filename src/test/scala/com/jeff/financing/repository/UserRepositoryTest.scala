package com.jeff.financing.repository

import org.junit.Test

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

class UserRepositoryTest {

  @Test
  def findOne(): Unit = {
    val f: Future[Option[User]] = UserRepository.findOne(20)
    f onComplete {
      case Success(value) => println(value)
      case Failure(exception) => exception.printStackTrace()
    }
    Thread.sleep(5000)
  }

  @Test
  def findByAge(): Unit = {
    val f: Future[Vector[User]] = UserRepository.findByAge(20)
    val result = Await.result(f, 10 second)
    println(result)
  }

  @Test def update(): Unit = {
    UserRepository.update("5f3e789b0b3e528767af9a4f", User("李四", 1, 30))
    Thread.sleep(10000)
  }

  @Test def create(): Unit = {
    UserRepository.create(User("张三", 1, 20))
    Thread.sleep(5000)
  }

}
