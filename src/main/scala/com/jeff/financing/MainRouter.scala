package com.jeff.financing

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.jeff.financing.api.{AccountRoter, IndexRoter}

object MainRouter {

  /**
   * 根路径
   */
  val ROOT_PATH = "financing";
  lazy val topLevelRoute: Route =
    concat(
      pathPrefix(ROOT_PATH)(IndexRoter.route),
      pathPrefix(ROOT_PATH)(AccountRoter.route)
    )

}

