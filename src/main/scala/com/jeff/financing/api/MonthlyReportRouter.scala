package com.jeff.financing.api

import akka.http.scaladsl.server.Directives._
import com.jeff.financing.dto.MonthlyReportItemJsonSupport._
import com.jeff.financing.service.MonthlyReportService

object MonthlyReportRouter {

  val service = new MonthlyReportService {}
  val route =
    path("monthly/report/previews") {
      get {
        parameters("start_date".as[Int], "end_date".as[Int]) { (startDate, endDate) =>
          complete(service.previews(startDate, endDate))
        }
      }
    }

}