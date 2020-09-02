package com.jeff.financing.entity

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jeff.financing.enums.Category
import com.jeff.financing.enums.Category.Category
import reactivemongo.api.bson.BSONObjectID
import spray.json.{DeserializationException, JsString, JsValue, RootJsonFormat}

/**
 *
 * 流水
 *
 * @param _id
 * @param platform   平台
 * @param category   类别 [[com.jeff.financing.enums.Category]]
 * @param state      状态 (1存入,0取出)
 * @param amount     金额(单位元)
 * @param rate       利率
 * @param target     标的
 * @param startTime  开始时间
 * @param endTime    到期时间
 * @param createTime 创建时间
 */
case class Flow(_id: Option[BSONObjectID], platform: Option[String], category: Category,
                state: Int, amount: Double, rate: Option[Float], target: String,
                startTime: Option[Long], endTime: Option[Long], createTime: Long)


object FlowJsonSupport extends ObjectIdSerialization with SprayJsonSupport {

  implicit object CategoryJsonFormat extends RootJsonFormat[Category] {
    def write(category: Category): JsValue = {
      new JsString(category.toString)
    }

    def read(jsValue: JsValue): Category = {
      jsValue match {
        case JsString(category) =>
          Category.withName(category)
        case _ =>
          throw DeserializationException("String expected")
      }
    }
  }

  implicit val flowFormats = jsonFormat10(Flow)
}
