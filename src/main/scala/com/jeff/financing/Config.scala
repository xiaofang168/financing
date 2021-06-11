package com.jeff.financing

import reactivemongo.api.{AsyncDriver, DB, FailoverStrategy, MongoConnection}
import zio.{Task, ZIO}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

object Config {

  val dbConfig: Task[DB] = {
    ZIO.fromFuture(_ => fdbConfig)
  }

  val fdbConfig: Future[DB] = {
    val driver = AsyncDriver()

    val defaultStrategy = FailoverStrategy()

    for {
      uri <- MongoConnection.fromString("mongodb://mongosiud:mongo123$%^@127.0.0.1:27017/mydb")
      con <- driver.connect(uri)
      dn <- Future(uri.db.get)
      db <- con.database(dn, defaultStrategy)
    } yield db
  }

}
