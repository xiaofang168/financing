package com.jeff.financing.repository

import com.jeff.financing.Config
import com.jeff.financing.entity.Persistence
import com.jeff.financing.repository.MongoExecutor.customStrategy
import org.reflections.Reflections
import reactivemongo.api._
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, document}
import zio.{Task, ZIO}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import scala.reflect.ClassTag

trait ZioMongoExecutor[T] {

  private def getCollName(className: String): String = {
    import scala.collection.mutable
    import scala.jdk.CollectionConverters._
    val annotation = new Reflections(className).getTypesAnnotatedWith(classOf[Persistence])
    val values: mutable.Set[Persistence] = annotation.asScala.map(e => e.getAnnotation(classOf[Persistence]))
    values.head.collName()
  }

  protected def exec[A](fn: BSONCollection => Future[A])(implicit tag: ClassTag[T]): Task[A] = {
    val collName = getCollName(tag.runtimeClass.getName)
    for {
      db <- Config.dbConfig
      r <- ZIO.fromFuture(_ => fn(db.collection(collName, customStrategy)))
    } yield r
    //ZIO.accessM(e => ZIO.fromFuture(_ => fn(e.collection(collName, customStrategy))))
  }

  def create(t: T)(implicit m: BSONDocumentWriter[T], tag: ClassTag[T]): Task[Boolean] = {
    exec(coll => {
      coll.insert.one(t).map(_.n > 0)
    })
  }

  def get(id: String)(implicit m: BSONDocumentReader[T], tag: ClassTag[T]): Task[Option[T]] = {
    val query = document("_id" -> BSONObjectID.parse(id).get)
    exec(coll => {
      coll.find(query, Option.empty[BSONDocument]).one[T]
    })
  }

  def findOne(findDoc: BSONDocument, sortDoc: BSONDocument = document("_id" -> -1))(implicit m: BSONDocumentReader[T], tag: ClassTag[T]): Task[Option[T]] = {
    exec(coll => {
      coll.find(findDoc, Option.empty[BSONDocument]).one[T]
    })
  }

  def findOne(findDoc: BSONDocument)(implicit m: BSONDocumentReader[T], tag: ClassTag[T]): Task[Option[T]] = {
    exec(coll => {
      coll.find(findDoc, Option.empty[BSONDocument]).one[T]
    })
  }

  def list(offset: Int, limit: Int, sortDoc: BSONDocument)(implicit m: BSONDocumentReader[T], tag: ClassTag[T]): Task[Vector[T]] = {
    this.list(offset, limit, document, sortDoc)
  }

  def list(offset: Int, limit: Int, findDoc: BSONDocument, sortDoc: BSONDocument)(implicit m: BSONDocumentReader[T], tag: ClassTag[T]): Task[Vector[T]] = {
    exec(coll => {
      coll.find(findDoc, Option.empty[BSONDocument])
        .sort(sortDoc)
        .skip(offset)
        .cursor[T](ReadPreference.primary).
        collect[Vector](limit, Cursor.FailOnError[Vector[T]]())
    })
  }

  def update(id: String, t: T)(implicit m: BSONDocumentWriter[T], tag: ClassTag[T]): Task[Int] = {
    val selector = document("_id" -> BSONObjectID.parse(id).get)
    exec(coll => {
      coll.update.one(selector, t, true).map(_.n)
    })
  }

  def delete(id: String)(implicit m: BSONDocumentWriter[T], tag: ClassTag[T]): Task[Int] = {
    val selector = document("_id" -> BSONObjectID.parse(id).get)
    exec(coll => {
      coll.delete.one(selector).map(_.n)
    })
  }

}
