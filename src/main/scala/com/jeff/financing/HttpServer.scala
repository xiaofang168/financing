package com.jeff.financing

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.util.Timeout
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import com.jeff.financing.ActorEnv.ActorEnv
import org.slf4j.{Logger, LoggerFactory}
import zio.{Task, ZIO, _}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

object HttpServer extends zio.App {

  private val logger: Logger = LoggerFactory.getLogger(HttpServer.getClass)

  private val bindTask: ActorSystem => Task[Future[Http.ServerBinding]] = { system =>
    implicit val timeout = Timeout(10 seconds)
    implicit val sys: ActorSystem = system
    implicit val executor: ExecutionContext = system.dispatcher
    val tt = Http().newServerAt("localhost", 8088).bind(MainRouter.topLevelRoute)
    Task(tt)
  }

  val program: ZIO[ZEnv, Throwable, Future[Http.ServerBinding]] =
    (for {
      _ <- Config.dbConfig.fold(f => logger.info(s"db failed to connect ${f.getMessage}"),
        s => logger.info(s"db connected $s"))
      systemT <- ZIO.accessM[ActorEnv](_.get.getActorSystem)
      b <- bindTask(systemT)
    } yield b
      ).provideLayer(ActorEnv.live)

  override def run(args: List[String]): URIO[ZEnv, ExitCode] = {
    // setting logback
    val lc = LoggerFactory.getILoggerFactory().asInstanceOf[LoggerContext]
    val configurator = new JoranConfigurator()
    configurator.setContext(lc)
    lc.reset()
    configurator.doConfigure(getClass.getResource("/logback.xml"))
    program.foldM(
      e => {
        logger.info(s"server failed to start ${e.getMessage}")
        IO.fail(throw e)
      },
      _ => {
        logger.info("server started...")
        IO.succeed(ExitCode.success).forever
      }
    )
  }

}
