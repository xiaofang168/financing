package com.jeff.financing.actor

import akka.actor.Actor
import com.jeff.financing.action.UserAction
import com.jeff.financing.entity.User
import com.jeff.financing.service.UserService

class UserActor extends Actor with UserService {

  override def receive: Receive = {
    case UserAction.Save(name, sex, age) => sender() ! save(User(name, sex, age))
    case UserAction.Get(id) => sender() ! get(id)
  }

}
