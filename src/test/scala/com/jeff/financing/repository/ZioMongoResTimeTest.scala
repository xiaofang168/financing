package com.jeff.financing.repository

import com.jeff.financing.Config
import com.jeff.financing.entity.MonthlyReport
import com.jeff.financing.repository.PersistenceImplicits._
import reactivemongo.api.DB
import reactivemongo.api.bson.document
import zio.{ExitCode, URIO, ZIO}

import scala.language.postfixOps

object ZioMongoResTimeTest extends zio.App {

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    (get(202105) *> get(202104)).exitCode
  }

  def get(date: Int): URIO[zio.ZEnv, ExitCode] = {
    val startTime = System.currentTimeMillis();
    val service = new ZioMongoExecutor[MonthlyReport]() {}
    val value1: ZIO[DB, Throwable, Option[MonthlyReport]] = service.findOne(document("date" -> date))
    (for {
      db <- Config.dbConfig
      a <- value1.provide(db)
    } yield a).fold(
      f => {
        println(s"fail f=$f");
      },
      s => {
        println(System.currentTimeMillis() - startTime)
        println(s"success res = $s");
      }
    ).exitCode
  }

}
