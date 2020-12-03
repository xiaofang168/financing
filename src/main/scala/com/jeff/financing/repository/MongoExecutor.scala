package com.jeff.financing.repository

import com.jeff.financing.entity.Persistence
import com.jeff.financing.repository.MongoExecutor.{customStrategy, db}
import org.reflections.Reflections
import reactivemongo.api._
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, document}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.reflect.ClassTag

trait MongoExecutor[T] {

  protected def exec[A](fn: BSONCollection => Future[A])(implicit tag: ClassTag[T]): Future[A] = {
    val collName = getCollName(tag.runtimeClass.getName)
    db.map(e => e.collection(collName, customStrategy)).flatMap(fn(_))
  }

  private def getCollName(className: String): String = {
    import scala.collection.mutable
    import scala.jdk.CollectionConverters._
    val annotation = new Reflections(className).getTypesAnnotatedWith(classOf[Persistence])
    val values: mutable.Set[Persistence] = annotation.asScala.map(e => e.getAnnotation(classOf[Persistence]))
    values.head.collName()
  }

  def create(t: T)(implicit m: BSONDocumentWriter[T], tag: ClassTag[T]): Future[Boolean] = {
    exec(coll => {
      coll.insert.one(t).map(_.n > 0)
    })
  }

  def get(id: String)(implicit m: BSONDocumentReader[T], tag: ClassTag[T]): Future[Option[T]] = {
    val query = document("_id" -> BSONObjectID.parse(id).get)
    exec(coll => {
      coll.find(query, Option.empty[BSONDocument]).one[T]
    })
  }

  def list(offset: Int, limit: Int, sortDoc: BSONDocument)(implicit m: BSONDocumentReader[T], tag: ClassTag[T]): Future[Vector[T]] = {
    this.list(offset, limit, document, sortDoc)
  }

  def list(offset: Int, limit: Int, findDoc: BSONDocument, sortDoc: BSONDocument)(implicit m: BSONDocumentReader[T], tag: ClassTag[T]): Future[Vector[T]] = {
    exec(coll => {
      coll.find(findDoc, Option.empty[BSONDocument])
        .sort(sortDoc)
        .skip(offset)
        .cursor[T](ReadPreference.primary).
        collect[Vector](limit, Cursor.FailOnError[Vector[T]]())
    })
  }

  def update(id: String, t: T)(implicit m: BSONDocumentWriter[T], tag: ClassTag[T]): Future[Int] = {
    val selector = document("_id" -> BSONObjectID.parse(id).get)
    exec(coll => {
      coll.update.one(selector, t, true).map(_.n)
    })
  }

  def delete(id: String)(implicit m: BSONDocumentWriter[T], tag: ClassTag[T]): Future[Int] = {
    val selector = document("_id" -> BSONObjectID.parse(id).get)
    exec(coll => {
      coll.delete.one(selector).map(_.n)
    })
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
