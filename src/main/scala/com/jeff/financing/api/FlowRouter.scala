package com.jeff.financing.api

import akka.http.scaladsl.server.Directives._
import com.jeff.financing.dto.FlowItemJsonSupport._
import com.jeff.financing.service.FlowService

object FlowRouter extends FlowService with CORSHandler {

  val route =
    path("flows" / Remaining) { id =>
      complete(get(id))
    } ~ path("flows") {
      corsHandler(complete(list()))
    }

}
