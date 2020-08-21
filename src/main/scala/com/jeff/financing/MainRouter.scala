package com.jeff.financing

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.jeff.financing.api.{IndexRoter, UserRoter}

object MainRouter {

  lazy val topLevelRoute: Route =
    concat(
      pathPrefix("financing")(IndexRoter.route),
      pathPrefix("financing")(UserRoter.route)
    )

}

