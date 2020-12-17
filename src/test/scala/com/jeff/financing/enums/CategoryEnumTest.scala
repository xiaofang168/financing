package com.jeff.financing.enums

import org.junit.Test

class CategoryTest {

  @Test
  def all(): Unit = {
    Category.values.foreach(println(_))
    Category.values.foreach(e => {
      println(Category.getDesc(e))
    })
  }

  @Test
  def index(): Unit = {
    Category.values.foreach(e => println(e.id))
  }

}
