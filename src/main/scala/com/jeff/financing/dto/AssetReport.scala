package com.jeff.financing.dto

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.jeff.financing.internal.LowerCaseWithUnderscores

case class AssetReport(dates: List[String],
                       stocks: List[BigDecimal],
                       stockFunds: List[BigDecimal],
                       indexFunds: List[BigDecimal],
                       bondFunds: List[BigDecimal],
                       monetaryFunds: List[BigDecimal],
                       insurances: List[BigDecimal],
                       banks: List[BigDecimal],
                       savings: List[BigDecimal])

object AssetReportJsonSupport extends LowerCaseWithUnderscores with SprayJsonSupport {
  implicit val assetReportFormats = jsonFormat9(AssetReport)
}