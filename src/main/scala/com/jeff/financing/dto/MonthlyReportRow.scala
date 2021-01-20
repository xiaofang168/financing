package com.jeff.financing.dto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jeff.financing.internal.LowerCaseWithUnderscores

final case class MonthlyReportRow(name: String,
                                  value: Option[String],
                                  group: String)

object MonthlyReportRowJsonSupport extends LowerCaseWithUnderscores with SprayJsonSupport {
  implicit val monthlyReportRowFormats = jsonFormat3(MonthlyReportRow)
}