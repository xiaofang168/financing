package com.jeff.financing.enums

import reactivemongo.api.bson.{BSONReader, BSONString, BSONValue, BSONWriter}

/**
 * 平台枚举
 */
object PlatformEnum extends Enumeration {
  type Platform = Value
  /**
   * 同花顺
   */
  val JQKA,

  /**
   * 阿里
   */
  ALI,

  /**
   * 天天基金
   */
  FUND,

  /**
   * 腾讯
   */
  TENCENT,

  /**
   * 百度
   */
  BAIDU,

  /**
   * 京东
   */
  JD,

  /**
   * 工行
   */
  ICBC,

  /**
   * 招行
   */
  CMB,

  /**
   * 中信
   */
  CITIC = Value

  def getDesc(platform: Platform): String = {
    platform match {
      case JQKA => "同花顺"
      case ALI => "阿里"
      case FUND => "天天基金"
      case TENCENT => "腾讯"
      case BAIDU => "百度"
      case JD => "京东"
      case ICBC => "工行"
      case CMB => "招行"
      case CITIC => "中信"
    }
  }

  implicit object PlatformReader extends BSONReader[Platform] {
    def readTry(bson: BSONValue) = bson.asTry[String].map(e => PlatformEnum.withName(e))
  }

  implicit object PlatformWriter extends BSONWriter[Platform] {
    def writeTry(category: Platform) =
      scala.util.Success(BSONString(category.toString))
  }

}
