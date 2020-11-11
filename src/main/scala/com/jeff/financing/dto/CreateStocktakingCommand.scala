package com.jeff.financing.dto

case class CreateStocktakingCommand(targetId: String, date: String, amount: BigDecimal, comment: Option[String])
