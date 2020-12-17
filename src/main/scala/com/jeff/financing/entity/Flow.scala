package com.jeff.financing.entity

import com.jeff.financing.enums.CategoryEnum.Category
import com.jeff.financing.enums.PlatformEnum.Platform
import reactivemongo.api.bson.BSONObjectID

/**
 *
 * 流水
 *
 * @param _id
 * @param platform   平台
 * @param category   类别 [[com.jeff.financing.enums.CategoryEnum]]
 * @param state      状态 (1存入,0取出)
 * @param amount     金额(单位元)
 * @param rate       利率
 * @param target     标的
 * @param startDate  开始时间yyyyMMdd
 * @param endDate    到期时间yyyyMMdd
 * @param createTime 创建时间
 */
@Persistence(collName = "flow")
case class Flow(_id: Option[BSONObjectID],
                platform: Platform,
                category: Category,
                state: Int,
                amount: BigDecimal,
                rate: Option[BigDecimal],
                target: String,
                startDate: Option[Int],
                endDate: Option[Int],
                createTime: Long = System.currentTimeMillis())
