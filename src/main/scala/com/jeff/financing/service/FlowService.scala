package com.jeff.financing.service

import com.jeff.financing.dto.FlowItem
import com.jeff.financing.entity.Flow
import com.jeff.financing.enums.Category
import com.jeff.financing.repository.FlowRepository
import com.jeff.financing.repository.PersistenceImplicits._
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait FlowService {

  def list(): Future[Vector[FlowItem]] = {
    val future: Future[Vector[Flow]] = FlowRepository.list()
    for {
      result <- future
    } yield {
      result.map(converter(_))
    }
  }

  def save(flow: Flow) = {
    FlowRepository.create(flow)
  }

  def get(id: String): Future[Option[FlowItem]] = {
    val future = FlowRepository.get(id)
    for {
      result <- future
    } yield {
      result.map(converter(_))
    }
  }

  private def converter(e: Flow): FlowItem = {
    val stateStr = if (e.state == 1) "存入" else "取出"
    FlowItem(e._id.get.stringify, e.platform, Category.getDesc(e.category), stateStr,
      e.amount, e.rate.map(e => String.valueOf((e * 100).underlying.stripTrailingZeros()) + "%"),
      e.dailyIncome, e.target,
      e.startTime.map(t => new DateTime(t).toString("yyyy-MM-dd")),
      e.endTime.map(t => new DateTime(t).toString("yyyy-MM-dd")),
      new DateTime(e.createTime).toString("yyyy-MM-dd"))
  }

}
