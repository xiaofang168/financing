package com.jeff.financing.service

import com.jeff.financing.entity.User
import com.jeff.financing.repository.UserRepository

import scala.concurrent.Future

trait UserService {

  def save(user: User): Future[Unit] = {
    UserRepository.create(user)
  }

  def get(id: String): Future[Option[User]] = {
    UserRepository.get(id)
  }

}
