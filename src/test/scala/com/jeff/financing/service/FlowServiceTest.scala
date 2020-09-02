package com.jeff.financing.service


import com.jeff.financing.entity.Flow
import com.jeff.financing.enums.Category
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.junit.Test

class FlowServiceTest {

  @Test
  def list(): Unit = {
    val flows = List(Flow(None, Some("蚂蚁财富"), Category.STOCK_FUND, 1, 10000, None,
      "新华轮换混合", Some(DateTime.parse("2020-03-06", DateTimeFormat.forPattern("yyyy-MM-dd")).getMillis),
      Some(DateTime.parse("2020-09-06", DateTimeFormat.forPattern("yyyy-MM-dd")).getMillis), System.currentTimeMillis()))
  }

}
