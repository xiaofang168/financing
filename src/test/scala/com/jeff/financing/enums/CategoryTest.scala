package com.jeff.financing.enums

import org.junit.Test

class CategoryTest {

  @Test
  def all(): Unit = {
    Category.values.foreach(println(_))
  }

  @Test
  def index(): Unit = {
    Category.values.foreach(e => println(e.id))
  }

}
