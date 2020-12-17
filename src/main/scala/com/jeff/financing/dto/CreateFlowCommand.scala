package com.jeff.financing.dto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jeff.financing.internal.LowerCaseWithUnderscores

case class CreateFlowCommand(platform: String, category: String, state: String,
                             amount: BigDecimal, rate: Option[BigDecimal],
                             target: String, startDate: Option[String], endDate: Option[String])

object CreateFlowCommandJsonSupport extends LowerCaseWithUnderscores with SprayJsonSupport {
  implicit val createFlowCommandFormats = jsonFormat8(CreateFlowCommand)
}