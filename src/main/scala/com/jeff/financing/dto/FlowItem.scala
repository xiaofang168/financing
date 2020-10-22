package com.jeff.financing.dto

case class FlowItem(id: String, platform: Option[String], category: String,
                    state: String, amount: Float, rate: Option[Float], dailyIncome: Option[Float],
                    target: String, startTime: Option[Long], endTime: Option[Long], createTime: Long)
