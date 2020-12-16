package com.jeff.financing.dto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jeff.financing.internal.LowerCaseWithUnderscores

case class FlowItem(id: String, platform: Option[String], category: String, categoryDesc: String,
                    state: Int, stateDesc: String, amount: BigDecimal, rate: Option[BigDecimal],
                    dailyIncome: Option[BigDecimal], days: Option[Int], allIncome: Option[BigDecimal],
                    stocktakingAmount: BigDecimal, target: String, startTime: Option[String],
                    endTime: Option[String], createTime: String)

object FlowItemJsonSupport extends LowerCaseWithUnderscores with SprayJsonSupport {
  implicit val flowItemFormats = jsonFormat16(FlowItem)
}