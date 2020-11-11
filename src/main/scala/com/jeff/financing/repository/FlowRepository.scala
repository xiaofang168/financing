package com.jeff.financing.repository

import com.jeff.financing.entity.Flow
import com.jeff.financing.repository.PersistenceImplicits._
import reactivemongo.api.bson.document

import scala.concurrent.Future

object FlowRepository extends MongoExecutor[Flow] {

  def list(): Future[Vector[Flow]] = {
    list(0, Int.MaxValue, document("endTime" -> 1))
  }

}
