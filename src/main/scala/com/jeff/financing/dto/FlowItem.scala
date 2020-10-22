package com.jeff.financing.dto

case class FlowItem(id: String, platform: Option[String], category: String,
                    state: String, amount: BigDecimal, rate: Option[BigDecimal], dailyIncome: Option[BigDecimal],
                    target: String, startTime: Option[Long], endTime: Option[Long], createTime: Long)
