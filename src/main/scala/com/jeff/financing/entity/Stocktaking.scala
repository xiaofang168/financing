package com.jeff.financing.entity

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import reactivemongo.api.bson.BSONObjectID

/**
 * 资产盘点
 *
 * @param _id
 * @param targetId   资产id
 * @param date       盘点日期yyyy-MM-dd
 * @param amount     金额
 * @param comment    备注
 * @param createTime 创建时间
 */
@Persistence(collName = "stocktaking")
case class Stocktaking(_id: Option[BSONObjectID], targetId: String, date: Long, amount: BigDecimal, comment: Option[String], createTime: Long)

object StocktakingJsonSupport extends ObjectIdSerialization with SprayJsonSupport {
  implicit val stocktakingFormats = jsonFormat6(Stocktaking)
}