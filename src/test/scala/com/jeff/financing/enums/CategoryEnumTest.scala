package com.jeff.financing.enums

import org.junit.Test

class CategoryEnumTest {

  @Test
  def all(): Unit = {
    CategoryEnum.values.foreach(println(_))
    CategoryEnum.values.foreach(e => {
      println(CategoryEnum.getDesc(e))
    })
  }

  @Test
  def index(): Unit = {
    CategoryEnum.values.foreach(e => println(e.id))
  }

}
