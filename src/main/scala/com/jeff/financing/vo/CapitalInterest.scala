package com.jeff.financing.vo

/**
 * 本息
 *
 * @param stock
 * @param stockFund
 * @param indexFund
 * @param bondFund
 * @param monetaryFund
 * @param insurance
 * @param bank
 * @param saving
 */
case class CapitalInterest(stock: Option[BigDecimal],
                           stockFund: Option[BigDecimal],
                           indexFund: Option[BigDecimal],
                           bondFund: Option[BigDecimal],
                           monetaryFund: Option[BigDecimal],
                           insurance: Option[BigDecimal],
                           bank: Option[BigDecimal],
                           saving: Option[BigDecimal]) extends Asset