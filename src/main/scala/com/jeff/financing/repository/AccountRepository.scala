package com.jeff.financing.repository

import com.jeff.financing.entity.Account
import com.jeff.financing.repository.PersistenceImplicits._
import reactivemongo.api.bson.{BSONDocument, document}
import reactivemongo.api.{Cursor, ReadPreference}
import zio.Task

import scala.concurrent.ExecutionContext.Implicits.global

object AccountRepository extends ZioMongoExecutor[Account] {

  def findOne(age: Int): Task[Option[Account]] = {
    val query = document("age" -> age)
    exec(coll => {
      coll.find(query, Option.empty[BSONDocument]).one[Account]
    })
  }

  def findByAge(age: Int): Task[Vector[Account]] = {
    val projection = Some(document("_id" -> 1, "name" -> 1, "sex" -> 1, "age" -> 1))
    exec(coll => {
      coll.find(document("age" -> age), projection).
        cursor[Account](ReadPreference.primary).
        collect[Vector](10, Cursor.FailOnError[Vector[Account]]()
        )
    })
  }

}
