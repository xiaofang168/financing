package com.jeff

import org.junit._

@Test
class AppTest {

  @Test
  def testOK() = {
    val b = BigDecimal(0.888200000)
    println((b * 100).underlying.stripTrailingZeros())
  }
}


