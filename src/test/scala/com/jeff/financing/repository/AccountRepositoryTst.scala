package com.jeff.financing.repository

import com.jeff.financing.entity.Account
import com.jeff.financing.repository.PersistenceImplicits._
import zio.test.Assertion._
import zio.test.{DefaultRunnableSpec, assert}

import scala.language.postfixOps

object AccountRepositoryTst extends DefaultRunnableSpec {

  override def spec = suite("AccountRepositorySpec")(

    testM("findOne correctly displays output") {
      for {
        output <- AccountRepository.get("606c0e8490dd604cb242eeff")
      } yield {
        println(output)
        assert(0)(equalTo(0))
      }
    },

    testM("find one by age") {
      for {
        output <- AccountRepository.findOne(20)
      } yield {
        println(output)
        assert(0)(equalTo(0))
      }
    },

    testM("find by age") {
      for {
        output <- AccountRepository.findByAge(20)
      } yield {
        println(output)
        assert(0)(equalTo(0))
      }
    },

    testM("update") {
      for {
        output <- AccountRepository.update("606c4e2790dd604cb242f4cb", Account("李四", Some(1), Some(30)))
      } yield {
        println(output)
        assert(0)(equalTo(0))
      }
    },

    testM("insert") {
      for {
        output <- AccountRepository.create(Account("李四", Some(1), Some(25)))
      } yield {
        println(output)
        assert(0)(equalTo(0))
      }
    }
  )

}
