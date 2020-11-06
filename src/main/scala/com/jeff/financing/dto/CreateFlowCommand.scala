package com.jeff.financing.dto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jeff.financing.internal.LowerCaseWithUnderscores

case class CreateFlowCommand(platform: Option[String], category: String, state: String,
                             amount: BigDecimal, rate: Option[BigDecimal], dailyIncome: Option[BigDecimal],
                             target: String, startTime: Option[String], endTime: Option[String])

object CreateFlowCommandJsonSupport extends LowerCaseWithUnderscores with SprayJsonSupport {
  implicit val createFlowCommandFormats = jsonFormat9(CreateFlowCommand)
}