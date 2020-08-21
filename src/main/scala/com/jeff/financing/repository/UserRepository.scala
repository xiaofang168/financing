package com.jeff.financing.repository

import reactivemongo.api.Cursor
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID, Macros, document}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class User(name: String, sex: Int, age: Int)

object UserRepository extends MongoExecutor {

  implicit val userReader: BSONDocumentReader[User] = Macros.reader[User]

  implicit val userWriter: BSONDocumentWriter[User] = Macros.writer[User]

  def create(user: User): Future[Unit] = {
    exec(coll => {
      coll.insert.one(user).map(_ => {})
    })
  }

  def update(id: String, user: User): Future[Int] = {
    val selector = document("_id" -> BSONObjectID.parse(id).get
    )
    exec(coll => {
      coll.update.one(selector, user, true).map(_.n)
    })
  }

  def findOne(age: Int): Future[Option[User]] = {
    val query = document("age" -> age)
    exec(coll => {
      coll.find(query, Option.empty[BSONDocument]).one[User]
    })
  }

  def findByAge(age: Int): Future[Vector[User]] = {
    val projection = Some(document("name" -> 1, "sex" -> 1, "age" -> 1))
    exec(coll => {
      coll.find(document("age" -> age), projection).
        cursor[User]().
        collect[Vector](10, Cursor.FailOnError[Vector[User]]()
        )
    })
  }

  override def getCollName(): String = "user"

}
