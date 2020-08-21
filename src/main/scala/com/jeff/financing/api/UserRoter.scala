package com.jeff.financing.api


import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import com.jeff.financing.BootStart._
import com.jeff.financing.action.UserAction
import com.jeff.financing.actor.UserActor

object UserRoter {

  val userActor: ActorRef = system.actorOf(Props[UserActor], "financing-user")

  val route =
    path("users" / Remaining) { id =>
      complete {
        // complete with serialized Future result
        (userActor ? UserAction.Get(id)).map(_.toString)
      }
    }

}
