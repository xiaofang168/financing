package com.jeff.financing.api

import akka.http.scaladsl.server.Directives._
import com.jeff.financing.dto.MonthlyReportItemJsonSupport._
import com.jeff.financing.service.MonthlyReportService

import scala.concurrent.Future

object MonthlyReportRouter {

  val service = new MonthlyReportService {}
  val route =
    path("monthly" / "report" / "previews") {
      get {
        parameters("start_date".as[Int], "end_date".as[Int]) { (startDate, endDate) =>
          complete(service.previews(startDate, endDate))
        }
      }
    } ~ path("monthly" / "report" / "gen" / IntNumber) { date =>
      get {
        import com.jeff.financing.internal.FutureConverterImplicits._
        val result: Future[Map[String, String]] = service.gen(date)
        complete(result)
      }
    } ~ path("monthly" / "report" / IntNumber) { date =>
      get {
        import com.jeff.financing.dto.MonthlyReportRowJsonSupport._
        complete(service.detail(date))
      }
    }

}