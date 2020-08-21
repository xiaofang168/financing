package com.jeff.financing

import akka.actor.ActorSystem
import akka.http.scaladsl.Http

import scala.concurrent.ExecutionContext

object App {

  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem("financing-system")
    implicit val executor: ExecutionContext = system.dispatcher
    Http().newServerAt("localhost", 8080).bind(MainRouter.routes)
  }

}
