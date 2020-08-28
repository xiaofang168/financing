package com.jeff.financing.repository

import com.jeff.financing.entity.Account
import org.junit.Test

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

class AccountRepositoryTest {

  @Test
  def findOne(): Unit = {
    val f: Future[Option[Account]] = UserRepository.findOne(20)
    f onComplete {
      case Success(value) => println(value)
      case Failure(exception) => exception.printStackTrace()
    }
    Thread.sleep(5000)
  }

  @Test
  def get(): Unit = {
    val f: Future[Option[Account]] = UserRepository.get("5f3f4091516c63fa7b618add")
    f onComplete {
      case Success(value) => println(value)
      case Failure(exception) => exception.printStackTrace()
    }
    Thread.sleep(5000)
  }

  @Test
  def findByAge(): Unit = {
    val f: Future[Vector[Account]] = UserRepository.findByAge(20)
    val result = Await.result(f, 10 second)
    println(result)
  }

  @Test def update(): Unit = {
    UserRepository.update("5f3e789b0b3e528767af9a4f", Account("李四", Some(1), Some(30)))
    Thread.sleep(10000)
  }

  @Test def create(): Unit = {
    UserRepository.create(Account("张三", Some(1), Some(20)))
    Thread.sleep(5000)
  }

}
