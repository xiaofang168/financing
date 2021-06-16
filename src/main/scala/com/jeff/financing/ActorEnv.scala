package com.jeff.financing

import akka.actor.ActorSystem
import zio.Task

trait ActorEnv {
  def dependencies: ActorEnv.Service
}

object ActorEnv {

  trait Service {
    def getActorSystem: Task[ActorSystem]
  }

}

trait ActorEnvLive extends ActorEnv {
  // once instance, but not description. If wrapped in task will be evaluated each time (call by name)
  private val system = ActorSystem("financing-system")
  val dependencies = new ActorEnv.Service {
    override def getActorSystem: Task[ActorSystem] = Task(system)
  }
}

object ActorEnvLive extends ActorEnvLive