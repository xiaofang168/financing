package com.jeff.financing.api

import akka.http.scaladsl.server.Directives._
import com.jeff.financing.dto.MonthlyReportItemJsonSupport._
import com.jeff.financing.service.MonthlyReportService

import scala.util.{Failure, Success}

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
        onComplete(service.gen(date)) {
          case Success(value) => {
            value._id match {
              case Some(id@_) => complete(Map("id" -> id.stringify))
              case None => complete(Map("id" -> ""))
            }
          }
          case Failure(ex) => complete(s"生成月报出错: ${ex.getMessage}")
        }
      }
    } ~ path("monthly" / "report" / IntNumber) { date =>
      get {
        import com.jeff.financing.dto.MonthlyReportRowJsonSupport._
        complete(service.detail(date))
      }
    }

}