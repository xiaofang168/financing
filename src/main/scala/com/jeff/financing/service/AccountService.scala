package com.jeff.financing.service

import com.jeff.financing.entity.Account
import com.jeff.financing.repository.AccountRepository
import com.jeff.financing.repository.PersistenceImplicits._
import zio.{Has, Task, ZIO, ZLayer}

/**
 * <pre>
 * From my experience it is a real joy to write code with ZIO,
 * especially in cases where you have complex concurrent/async requirements which are
 * much simpler to implement with the features this library provides
 * </pre>
 */
object ZAccount {

  type ZAccountEnv = Has[ZAccount.Service]

  trait Service {
    def get(id: String): Task[Option[Account]]

    def save(user: Account): Task[Boolean]
  }

  val live: ZLayer[Any, Nothing, ZAccountEnv] = ZLayer.succeed(new Service {
    override def get(id: String): Task[Option[Account]] = AccountRepository.get(id)

    override def save(user: Account): Task[Boolean] = AccountRepository.create(user)
  })

  def get(id: String): ZIO[ZAccountEnv, Throwable, Option[Account]] = ZIO.accessM(_.get.get(id))

  def save(user: Account): ZIO[ZAccountEnv, Throwable, Boolean] = ZIO.accessM(_.get.save(user))
}
