package com.jeff


import org.joda.time.{DateTime, Days}
import org.junit._


@Test
class AppTest {

  @Test
  def testOK() = {
    val b = BigDecimal(0.888200000)
    println((b * 100).underlying.stripTrailingZeros())
  }

  @Test
  def dasy(): Unit = {
    val days = Days.daysBetween(new DateTime(1583424000000L), new DateTime(1599321600000l)).getDays
    println(days)
  }

}


