package com.jeff.financing.service

import com.jeff.financing.dto.FlowItem
import com.jeff.financing.entity.Flow
import com.jeff.financing.enums.Category
import com.jeff.financing.repository.FlowRepository
import com.jeff.financing.repository.PersistenceImplicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait FlowService {

  def list(): Future[Vector[FlowItem]] = {
    val future: Future[Vector[Flow]] = FlowRepository.list()
    for {
      result <- future
    } yield {
      result.map(e => {
        val stateStr = if (e.state == 1) "存入" else "取出"
        FlowItem(e._id.get.stringify, e.platform, Category.getDesc(e.category), stateStr, e.amount, e.rate, e.dailyIncome, e.target, e.startTime, e.endTime, e.createTime)
      })
    }
  }

  def save(flow: Flow) = {
    FlowRepository.create(flow)
  }

}
