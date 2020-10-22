package com.jeff.financing

import akka.http.scaladsl.server.Directives.{pathPrefix, _}
import akka.http.scaladsl.server.Route
import com.jeff.financing.api.{AccountRouter, FlowRouter, IndexRouter}

object MainRouter {

  /**
   * 根路径
   */
  val ROOT_PATH = "financing";
  lazy val topLevelRoute: Route =
    concat(
      pathPrefix(ROOT_PATH)(IndexRouter.route),
      pathPrefix(ROOT_PATH)(AccountRouter.route),
      pathPrefix(ROOT_PATH)(FlowRouter.route)
    )

}

