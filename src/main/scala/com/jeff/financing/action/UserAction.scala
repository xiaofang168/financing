package com.jeff.financing.action

object UserAction {

  case class Save(name: String, sex: Option[Int], age: Option[Int])

  case class Get(id: String)

}
