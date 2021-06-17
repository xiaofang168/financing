package com.jeff.financing

import akka.actor.ActorSystem
import zio.{Has, Task, ZLayer}

object ActorEnv {

  type ActorEnv = Has[ActorEnv.Service]

  private val system = ActorSystem("financing-system")

  trait Service {
    def getActorSystem: Task[ActorSystem]
  }

  val live: ZLayer[Any, Nothing, ActorEnv] = ZLayer.succeed(new Service {
    override def getActorSystem: Task[ActorSystem] = Task(system)
  })

}

