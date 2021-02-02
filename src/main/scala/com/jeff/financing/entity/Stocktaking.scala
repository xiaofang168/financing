package com.jeff.financing.entity

import reactivemongo.api.bson.BSONObjectID

/**
 * 资产盘点
 *
 * @param _id
 * @param targetId    资产id
 * @param date        盘点日期yyyyMM,例如202012
 * @param amount      金额
 * @param income      收益
 * @param totalIncome 累计收益
 * @param rate        利率
 * @param comment     备注
 * @param createTime  创建时间
 */
@Persistence(collName = "stocktaking")
case class Stocktaking(_id: Option[BSONObjectID],
                       targetId: String,
                       date: Int,
                       amount: BigDecimal,
                       income: BigDecimal = 0,
                       totalIncome: Option[BigDecimal] = None,
                       rate: Option[BigDecimal] = None,
                       comment: Option[String] = None,
                       createTime: Long = System.currentTimeMillis())