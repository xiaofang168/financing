package com.jeff.financing.dto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jeff.financing.internal.LowerCaseWithUnderscores

final case class CreateStocktakingCommand(targetId: String,
                                          date: String,
                                          amount: BigDecimal,
                                          income: BigDecimal,
                                          rate: Option[BigDecimal],
                                          comment: Option[String])

object CreateStocktakingJsonSupport extends LowerCaseWithUnderscores with SprayJsonSupport {
  implicit val createStocktakingFormats = jsonFormat6(CreateStocktakingCommand)
}