package com.jeff.financing.entity

import reactivemongo.api.bson.BSONObjectID

@Persistence(collName = "account")
case class Account(_id: Option[BSONObjectID], name: String, sex: Option[Int], age: Option[Int])

object Account {
  def apply(name: String, sex: Option[Int], age: Option[Int]) = new Account(None, name, sex, age)

  def apply(_id: Option[BSONObjectID], name: String, sex: Option[Int], age: Option[Int]) = new Account(_id, name, sex, age)
}

