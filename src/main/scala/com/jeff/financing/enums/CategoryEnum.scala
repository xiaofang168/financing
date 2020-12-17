package com.jeff.financing.enums

import reactivemongo.api.bson.{BSONReader, BSONString, BSONValue, BSONWriter}

object Category extends Enumeration {
  type Category = Value
  /**
   * 股票
   */
  val STOCK,

  /**
   * 股票基金
   */
  STOCK_FUND,

  /**
   * 指数基金
   */
  INDEX_FUND,

  /**
   * 债券基金
   */
  BOND_FUND,

  /**
   * 货币基金
   */
  MONETARY_FUND,

  /**
   * 保险
   */
  INSURANCE,

  /**
   * 银行理财
   */
  BANK,

  /**
   * 储蓄
   */
  SAVING = Value

  def getDesc(category: Category): String = {
    category match {
      case STOCK => "股票"
      case STOCK_FUND => "股票基金"
      case INDEX_FUND => "指数基金"
      case BOND_FUND => "债券基金"
      case MONETARY_FUND => "货币基金"
      case INSURANCE => "保险"
      case BANK => "银行理财"
      case SAVING => "银行储蓄"
    }
  }

  implicit object CategoryReader extends BSONReader[Category] {
    def readTry(bson: BSONValue) = bson.asTry[String].map(e => Category.withName(e))
  }

  implicit object CategoryWriter extends BSONWriter[Category] {
    def writeTry(category: Category) =
      scala.util.Success(BSONString(category.toString))
  }

}