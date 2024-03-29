package com.jeff.financing.dto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jeff.financing.internal.LowerCaseWithUnderscores

final case class CreateFlowCommand(platform: String,
                                   category: String,
                                   amount: BigDecimal,
                                   rate: Option[BigDecimal],
                                   target: String,
                                   startDate: String,
                                   endDate: Option[String])

object CreateFlowCommandJsonSupport extends LowerCaseWithUnderscores with SprayJsonSupport {
  implicit val createFlowCommandFormats = jsonFormat7(CreateFlowCommand)
}