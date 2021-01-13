package com.jeff.financing.dto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jeff.financing.internal.LowerCaseWithUnderscores

case class StocktakingItem(targetId: String,
                           id: String,
                           date: String,
                           amount: BigDecimal,
                           rate: Option[BigDecimal],
                           createTime: String,
                           comment: Option[String])

object StocktakingItemJsonSupport extends LowerCaseWithUnderscores with SprayJsonSupport {
  implicit val stocktakingItemFormats = jsonFormat7(StocktakingItem)
}