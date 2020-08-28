package com.jeff.financing.repository

import com.jeff.financing.entity.Account
import reactivemongo.api.Cursor
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, Macros, document}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object UserRepository extends MongoExecutor {

  implicit val userReader: BSONDocumentReader[Account] = Macros.reader[Account]

  implicit val userWriter: BSONDocumentWriter[Account] = Macros.writer[Account]

  def create(user: Account): Future[Unit] = {
    exec(coll => {
      coll.insert.one(user).map(_ => {})
    })
  }

  def update(id: String, user: Account): Future[Int] = {
    val selector = document("_id" -> BSONObjectID.parse(id).get
    )
    exec(coll => {
      coll.update.one(selector, user, true).map(_.n)
    })
  }

  def get(id: String): Future[Option[Account]] = {
    val query = document("_id" -> BSONObjectID.parse(id).get)
    exec(coll => {
      coll.find(query, Option.empty[BSONDocument]).one[Account]
    })
  }

  def findOne(age: Int): Future[Option[Account]] = {
    val query = document("age" -> age)
    exec(coll => {
      coll.find(query, Option.empty[BSONDocument]).one[Account]
    })
  }

  def findByAge(age: Int): Future[Vector[Account]] = {
    val projection = Some(document("name" -> 1, "sex" -> 1, "age" -> 1))
    exec(coll => {
      coll.find(document("age" -> age), projection).
        cursor[Account]().
        collect[Vector](10, Cursor.FailOnError[Vector[Account]]()
        )
    })
  }

  override def getCollName(): String = "account"

}
