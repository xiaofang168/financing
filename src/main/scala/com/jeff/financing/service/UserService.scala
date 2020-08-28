package com.jeff.financing.service

import com.jeff.financing.entity.Account
import com.jeff.financing.repository.UserRepository

import scala.concurrent.Future

trait UserService {

  def save(user: Account): Future[Unit] = {
    UserRepository.create(user)
  }

  def get(id: String): Future[Option[Account]] = {
    UserRepository.get(id)
  }

}
