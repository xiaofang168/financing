package com.jeff.financing.repository

import com.jeff.financing.Config
import com.jeff.financing.entity.Account
import com.jeff.financing.repository.PersistenceImplicits._
import reactivemongo.api.DB
import zio.{ExitCode, URIO, ZIO}

import scala.language.postfixOps

object ZioMongoExecutorTest extends zio.App {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    val service = new ZioMongoExecutor[Account]() {}
    val value1: ZIO[DB, Throwable, Option[Account]] = service.get("606c0e8490dd604cb242eeff")
    (for {
      db <- Config.dbConfig
      a <- value1.provide(db)
    } yield a).fold(
      f => {
        println(s"fail f=$f");
      },
      s => {
        println(s"success res = $s");
      }
    ).exitCode
  }

}
