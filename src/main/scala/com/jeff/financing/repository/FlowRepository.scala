package com.jeff.financing.repository

import com.jeff.financing.entity.Flow
import com.jeff.financing.repository.PersistenceImplicits._

import scala.concurrent.Future

object FlowRepository extends MongoExecutor[Flow] {
  override def getCollName(): String = "flow"

  def list(): Future[Vector[Flow]] = {
    list(0, Int.MaxValue)
  }

}
