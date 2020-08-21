package com.jeff.financing.repository

import com.jeff.financing.repository.MongoExecutor.{customStrategy, db}
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.{AsyncDriver, DB, FailoverStrategy, MongoConnection}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps

trait MongoExecutor {
  def getCollName(): String

  def exec[T](fn: BSONCollection => Future[T]): Future[T] = {
    db.map(e => e.collection(getCollName(), customStrategy)).flatMap(fn(_))
  }
}

object MongoExecutor {

  // Connect to the database: Must be done only once per application
  val driver = AsyncDriver()

  // My settings (see available connection options)
  val mongoUri = "mongodb://mongosiud:mongo123$%^@127.0.0.1:27017/mydb"

  val defaultStrategy = FailoverStrategy()

  val customStrategy = FailoverStrategy(
    initialDelay = 500 milliseconds,
    retries = 5,
    delayFactor = attemptNumber => 1 + attemptNumber * 0.5
  )

  val db: Future[DB] = for {
    uri <- MongoConnection.fromString(mongoUri)
    con <- driver.connect(uri)
    dn <- Future(uri.db.get)
    db <- con.database(dn, defaultStrategy)
  } yield db

}
