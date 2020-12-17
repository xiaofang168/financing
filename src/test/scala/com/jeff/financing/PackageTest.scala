package com.jeff.financing

import org.junit.{Assert, Test}

class PackageTest {

  @Test
  def str2Int(): Unit = {
    val date: Option[Int] = com.jeff.financing.str2Int(Some("2020-12)))/-20"))
    Assert.assertTrue(date == Some(20201220))
  }

}
