package com.jeff.financing.dto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jeff.financing.internal.LowerCaseWithUnderscores

case class FlowItem(id: String,
                    platform: String,
                    platformDesc: String,
                    category: String,
                    categoryDesc: String,
                    state: Int,
                    stateDesc: String,
                    amount: BigDecimal,
                    rate: Option[BigDecimal],
                    dailyIncome: Option[BigDecimal],
                    days: Option[Int],
                    allIncome: Option[BigDecimal],
                    stocktakingAmount: BigDecimal,
                    target: String,
                    startDate: Option[Int],
                    endDate: Option[Int],
                    createTime: String)

object FlowItemJsonSupport extends LowerCaseWithUnderscores with SprayJsonSupport {
  implicit val flowItemFormats = jsonFormat17(FlowItem)
}