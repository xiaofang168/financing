package com.jeff.financing

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.util.Timeout
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.util.StatusPrinter
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

object BootStart extends App {
  implicit val timeout = Timeout(10 seconds)
  implicit val system = ActorSystem("financing-system")
  implicit val executor: ExecutionContext = system.dispatcher
  // setting logback
  val lc = LoggerFactory.getILoggerFactory().asInstanceOf[LoggerContext]
  val configurator = new JoranConfigurator()
  configurator.setContext(lc)
  lc.reset()
  configurator.doConfigure(getClass.getResource("/logback.xml"))
  StatusPrinter.printInCaseOfErrorsOrWarnings(lc)
  Http().newServerAt("localhost", 8088).bind(MainRouter.topLevelRoute)
}