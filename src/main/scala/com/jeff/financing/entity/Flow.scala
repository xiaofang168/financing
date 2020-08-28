package com.jeff.financing.entity

import com.jeff.financing.enums.Category.Category

/**
 *
 * 流水
 *
 * @param id
 * @param platform   平台
 * @param category   类别 [[com.jeff.financing.enums.Category]]
 * @param state      状态 (1存入,0取出)
 * @param amount     金额(单位元)
 * @param rate       利率
 * @param desc       备注/描述
 * @param startTime  开始时间
 * @param endTime    到期时间
 * @param createTime 创建时间
 */
case class Flow(id: Option[String], platform: Option[String], category: Category,
                state: Int, amount: Double, rate: Double, desc: Option[String],
                startTime: Option[Long], endTime: Option[Long], createTime: Option[Long])
