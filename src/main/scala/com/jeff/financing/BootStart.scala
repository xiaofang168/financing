package com.jeff.financing

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

object BootStart extends App {
  implicit val timeout = Timeout(10 seconds)
  implicit val system = ActorSystem("financing-system")
  implicit val executor: ExecutionContext = system.dispatcher
  Http().newServerAt("localhost", 8080).bind(MainRouter.topLevelRoute)
}
