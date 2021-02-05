package com.jeff.financing

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import ch.qos.logback.core.util.StatusPrinter
import org.slf4j.LoggerFactory
import zio.internal.Platform
import zio.{ExitCode, IO, Runtime, Task, URIO, ZEnv, ZIO, console, _}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

object BootStart extends App {
  private val runtime = Runtime(ActorEnvLive, Platform.default)

  private val bindTask: ActorSystem => Task[Future[Http.ServerBinding]] = { system =>
    implicit val sys: ActorSystem = system
    implicit val executor: ExecutionContext = system.dispatcher
    // setting logback
    val lc = LoggerFactory.getILoggerFactory().asInstanceOf[LoggerContext]
    val configurator = new JoranConfigurator()
    configurator.setContext(lc)
    lc.reset()
    configurator.doConfigure(getClass.getResource("/logback.xml"))
    StatusPrinter.printInCaseOfErrorsOrWarnings(lc)
    Task(Http().newServerAt("localhost", 8088).bind(MainRouter.topLevelRoute))
  }

  val program: ZIO[ZEnv, Throwable, Future[Http.ServerBinding]] =
    (for {
      systemT <- ZIO.access[ActorEnv](_.dependencies.getActorSystem)
      b <- systemT.flatMap { s => bindTask(s) }
    } yield b
      ).provide(ActorEnvLive)

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = program.foldM(
    e => {
      console.putStrLn(s"Server failed to start ${e.getMessage}") *> IO.fail(throw e)
    },
    _ => {
      (console.putStrLn("Server started...") *> IO.succeed(0) *> IO.unit.forever).run.exitCode
    }
  )
}
