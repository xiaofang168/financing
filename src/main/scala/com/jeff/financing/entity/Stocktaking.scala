package com.jeff.financing.entity

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jeff.financing.internal.LowerCaseWithUnderscores
import reactivemongo.api.bson.BSONObjectID

/**
 * 资产盘点
 *
 * @param _id
 * @param targetId   资产id
 * @param date       盘点日期yyyyMM,例如202012
 * @param amount     金额
 * @param comment    备注
 * @param createTime 创建时间
 */
@Persistence(collName = "stocktaking")
case class Stocktaking(_id: Option[BSONObjectID], targetId: String, date: Int, amount: BigDecimal, comment: Option[String], createTime: Long)

object StocktakingJsonSupport extends ObjectIdSerialization with LowerCaseWithUnderscores with SprayJsonSupport {
  implicit val stocktakingFormats = jsonFormat6(Stocktaking)
}

