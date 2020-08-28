package com.jeff.financing.entity

/**
 * 资产盘点
 *
 * @param id
 * @param amount     金额
 * @param desc       描述
 * @param date       盘点日期yyyy-MM-dd
 * @param createTime 创建时间
 */
case class Inventory(id: Option[String], amount: Double, desc: Option[String], date: String, createTime: Long)
