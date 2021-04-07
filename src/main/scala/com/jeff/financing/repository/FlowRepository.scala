package com.jeff.financing.repository


import com.jeff.financing.entity.Flow
import com.jeff.financing.repository.PersistenceImplicits._
import com.jeff.financing.{getBson, str2Int}
import reactivemongo.api.bson._
import zio.Task

object FlowRepository extends ZioMongoExecutor[Flow] {

  def list(): Task[Vector[Flow]] = {
    list(0, Int.MaxValue, document("endDate" -> 1))
  }

  def list(startDate: Option[String], endDate: Option[String], platform: Option[String], category: Option[String]): Task[Vector[Flow]] = {
    // 转换查询需要的数据格式
    val map = Map("startDate" -> str2Int(startDate), "endDate" -> str2Int(endDate),
      "platform" -> platform.flatMap {
        case "" => None
        case c@_ => Some(c)
      },
      "category" -> category.flatMap {
        case "" => None
        case c@_ => Some(c)
      })
      .filter { case (_, v) => v.isDefined }
      .map {
        case (key, Some(value)) => key -> value
      }
    val doc = BSONDocument(map.map {
      case (k, v) => {
        if ("startDate".equals(k)) {
          (k, BSONDocument("$gte" -> getBson(v)))
        } else if ("endDate".eq(k)) {
          (k, BSONDocument("$lte" -> getBson(v)))
        }
        else {
          (k, getBson(v))
        }
      }
    })

    list(0, Int.MaxValue, doc, document("endDate" -> 1))
  }

}
