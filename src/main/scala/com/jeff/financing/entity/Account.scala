package com.jeff.financing.entity

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class Account(id: Option[String], name: String, sex: Option[Int], age: Option[Int])

object Account {
  def apply(name: String, sex: Option[Int], age: Option[Int]) = new Account(None, name, sex, age)

  def apply(id: Option[String], name: String, sex: Option[Int], age: Option[Int]) = new Account(id: Option[String], name: String, sex: Option[Int], age: Option[Int])
}

object AccountJsonSupport extends DefaultJsonProtocol with SprayJsonSupport {
  implicit val accountFormats = jsonFormat4(Account.apply)
}

