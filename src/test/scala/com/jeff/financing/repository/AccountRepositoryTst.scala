package com.jeff.financing.repository

import com.jeff.financing.entity.Account
import com.jeff.financing.repository.PersistenceImplicits._
import org.junit.Test
import zio.Runtime

import scala.language.postfixOps

class AccountRepositoryTst {

  @Test
  def findOne(): Unit = {
    Runtime.default.unsafeRun(AccountRepository.findOne(20)
      .fold(
        f => {
          println(s"fail f=$f");
        },
        s => {
          println(s"success res = $s");
        }
      ))
  }

  @Test
  def get(): Unit = {
    Runtime.default.unsafeRun(AccountRepository.get("606c0e8490dd604cb242eeff")
      .fold(
        f => {
          println(s"fail f=$f");
        },
        s => {
          println(s"success res = $s");
        }
      ))
  }

  @Test
  def findByAge(): Unit = {
    Runtime.default.unsafeRun(AccountRepository.findByAge(20)
      .fold(
        f => {
          println(s"fail f=$f");
        },
        s => {
          println(s"success res = $s");
        }
      ))
  }

  @Test def update(): Unit = {
    Runtime.default.unsafeRun(AccountRepository.update("5f3e789b0b3e528767af9a4f", Account("李四", Some(1), Some(30)))
      .fold(
        f => {
          println(s"fail f=$f");
        },
        s => {
          println(s"success res = $s");
        }
      ))
  }

  @Test def create(): Unit = {
    Runtime.default.unsafeRun(AccountRepository.create(Account("李四", Some(1), Some(25)))
      .fold(
        f => {
          println(s"fail f=$f");
        },
        s => {
          println(s"success res = $s");
        }
      ))
  }

}
