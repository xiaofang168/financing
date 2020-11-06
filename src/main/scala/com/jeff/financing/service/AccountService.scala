package com.jeff.financing.service

import com.jeff.financing.entity.Account
import com.jeff.financing.repository.AccountRepository
import com.jeff.financing.repository.PersistenceImplicits._

import scala.concurrent.Future

trait AccountService {

  def save(user: Account): Future[Boolean] = {
    AccountRepository.create(user)
  }

  def get(id: String): Future[Option[Account]] = {
    AccountRepository.get(id)
  }

}
