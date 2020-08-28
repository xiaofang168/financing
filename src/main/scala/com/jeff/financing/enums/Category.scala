package com.jeff.financing.enums


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
}