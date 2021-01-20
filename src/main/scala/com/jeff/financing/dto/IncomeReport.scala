package com.jeff.financing.dto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jeff.financing.internal.LowerCaseWithUnderscores

/**
 *
 * @param dates            日期集合
 * @param capitals         本金集合
 * @param capitalInterests 本息集合
 * @param incomes          收益集合
 */
final case class IncomeReport(dates: List[String],
                              capitals: List[BigDecimal],
                              capitalInterests: List[BigDecimal],
                              incomes: List[BigDecimal])

object IncomeReportJsonSupport extends LowerCaseWithUnderscores with SprayJsonSupport {
  implicit val incomeReportFormats = jsonFormat4(IncomeReport)
}
