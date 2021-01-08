package com.jeff.financing.vo

trait Asset {
  def stock: Option[BigDecimal]

  def stockFund: Option[BigDecimal]

  def indexFund: Option[BigDecimal]

  def bondFund: Option[BigDecimal]

  def monetaryFund: Option[BigDecimal]

  def insurance: Option[BigDecimal]

  def bank: Option[BigDecimal]

  def saving: Option[BigDecimal]
}
