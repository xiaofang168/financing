package com.jeff.financing.api

import akka.http.scaladsl.server.Directives.{path, _}
import com.jeff.financing.dto.ZioSupport._
import com.jeff.financing.dto.{AssetReport, IncomeReport, MonthlyReportItem, MonthlyReportRow}
import com.jeff.financing.service.{ZFlow, ZMonthlyReport, ZStocktaking}
import zio.Task

object MonthlyReportRouter {

  val monthlyReportLayer = (ZStocktaking.live >>> ZFlow.live) >>> ZMonthlyReport.live

  val route =
    path("monthly" / "report" / "previews") {
      get {
        parameters("start_date".as[Int], "end_date".as[Int]) { (startDate, endDate) =>
          import com.jeff.financing.dto.MonthlyReportItemJsonSupport._
          val result: Task[List[MonthlyReportItem]] = ZMonthlyReport.previews(startDate, endDate)
                                                                    .provideLayer(monthlyReportLayer)
          complete(result)
        }
      }
    } ~ path("monthly" / "report" / "income") {
      get {
        parameters("start_date".as[Int], "end_date".as[Int]) { (startDate, endDate) =>
          import com.jeff.financing.dto.IncomeReportJsonSupport._
          val result: Task[IncomeReport] = ZMonthlyReport.findIncome(startDate, endDate)
                                                         .provideLayer(monthlyReportLayer)
          complete(result)
        }
      }
    } ~ path("monthly" / "report" / "asset" / "capital") {
      get {
        parameters("start_date".as[Int], "end_date".as[Int]) { (startDate, endDate) =>
          import com.jeff.financing.dto.AssetReportJsonSupport._
          val result: Task[AssetReport] = ZMonthlyReport.findAssert(startDate, endDate, 0)
                                                        .provideLayer(monthlyReportLayer)
          complete(result)
        }
      }
    } ~ path("monthly" / "report" / "asset" / "capital_interests") {
      get {
        parameters("start_date".as[Int], "end_date".as[Int]) { (startDate, endDate) =>
          import com.jeff.financing.dto.AssetReportJsonSupport._
          val result: Task[AssetReport] = ZMonthlyReport.findAssert(startDate, endDate, 1)
                                                        .provideLayer(monthlyReportLayer)
          complete(result)
        }
      }
    } ~ path("monthly" / "report" / "gen" / IntNumber) { date =>
      get {
        import com.jeff.financing.dto.ZioSupport.JsonResultSupport._
        val result: Task[Boolean] = ZMonthlyReport.gen(date)
                                                  .provideLayer(monthlyReportLayer)
        complete(result.toJson)
      }
    } ~ path("monthly" / "report" / IntNumber) { date =>
      get {
        import com.jeff.financing.dto.MonthlyReportRowJsonSupport._
        val result: Task[List[MonthlyReportRow]] = ZMonthlyReport.detail(date)
                                                                 .provideLayer(monthlyReportLayer)
        complete(result)
      }
    }

}