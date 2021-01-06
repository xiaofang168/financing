package com.jeff.financing.dto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jeff.financing.internal.LowerCaseWithUnderscores

/**
 * 月报
 *
 * @param id    主键
 * @param date  日期yyyyMM
 * @param isGen 是否生成0否,1是
 */
case class MonthlyReportItem(id: String,
                             date: Int,
                             isGen: Int)

object MonthlyReportItemJsonSupport extends LowerCaseWithUnderscores with SprayJsonSupport {
  implicit val monthlyReportItemFormats = jsonFormat3(MonthlyReportItem)
}