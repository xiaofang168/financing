package com.jeff.financing

import akka.http.scaladsl.server.Directives.{pathPrefix, _}
import akka.http.scaladsl.server.Route
import com.jeff.financing.api._

object MainRouter {

  /**
   * 根路径
   */
  val ROOT_PATH = "financing"
  val API_PATH = "api";
  lazy val topLevelRoute: Route =
    pathPrefix(ROOT_PATH) {
      pathPrefix(API_PATH) {
        IndexRouter.route ~ AccountRouter.route ~ FlowRouter.route ~ StocktakingRouter.route ~ MonthlyReportRouter.route
      }
    }

}

