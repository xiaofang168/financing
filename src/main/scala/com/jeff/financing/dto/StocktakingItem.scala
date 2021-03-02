package com.jeff.financing.dto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jeff.financing.internal.LowerCaseWithUnderscores

final case class StocktakingItem(targetId: String,
                                 id: String,
                                 date: String,
                                 amount: BigDecimal,
                                 income: BigDecimal,
                                 totalIncome: Option[BigDecimal],
                                 rate: Option[BigDecimal],
                                 createTime: String,
                                 comment: Option[String])

object StocktakingItemJsonSupport extends LowerCaseWithUnderscores with SprayJsonSupport {
  implicit val stocktakingItemFormats = jsonFormat9(StocktakingItem)
}