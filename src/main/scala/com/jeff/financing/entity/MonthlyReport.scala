package com.jeff.financing.entity

import com.jeff.financing.vo.{Capital, CapitalInterest, Income}
import reactivemongo.api.bson.BSONObjectID

/**
 * 平台及类别月报
 *
 * @param _id
 * @param date            日期
 * @param capital         本金
 * @param capitalInterest 本息
 * @param income          月收益
 * @param createTime      创建时间
 */
@Persistence(collName = "monthly_report")
case class MonthlyReport(_id: Option[BSONObjectID],
                         date: Int,
                         capital: Capital,
                         capitalInterest: CapitalInterest,
                         income: Income,
                         createTime: Long = System.currentTimeMillis())
