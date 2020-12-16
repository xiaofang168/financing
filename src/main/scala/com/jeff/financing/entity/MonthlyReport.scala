package com.jeff.financing.entity

import reactivemongo.api.bson.BSONObjectID

/**
 * 平台及类别月报
 *
 * @param _id
 * @param date
 * @param stockAmount
 * @param stockIncome
 * @param stockFundAmount
 * @param stockFundIncome
 * @param indexFundAmount
 * @param indexFundIncome
 * @param bondFundAmount
 * @param bondFundIncome
 * @param monetaryFundAmount
 * @param monetaryFundIncome
 * @param insuranceAmount
 * @param insuranceIncome
 * @param bankAmount
 * @param bankIncome
 * @param savingAmount
 * @param savingIncome
 * @param createTime
 */
@Persistence(collName = "monthly_report")
case class MonthlyReport(_id: Option[BSONObjectID],
                         date: Int,
                         stockAmount: Option[BigDecimal],
                         stockIncome: Option[BigDecimal],
                         stockFundAmount: Option[BigDecimal],
                         stockFundIncome: Option[BigDecimal],
                         indexFundAmount: Option[BigDecimal],
                         indexFundIncome: Option[BigDecimal],
                         bondFundAmount: Option[BigDecimal],
                         bondFundIncome: Option[BigDecimal],
                         monetaryFundAmount: Option[BigDecimal],
                         monetaryFundIncome: Option[BigDecimal],
                         insuranceAmount: Option[BigDecimal],
                         insuranceIncome: Option[BigDecimal],
                         bankAmount: Option[BigDecimal],
                         bankIncome: Option[BigDecimal],
                         savingAmount: Option[BigDecimal],
                         savingIncome: Option[BigDecimal],
                         createTime: Long)
