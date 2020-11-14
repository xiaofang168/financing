package com.jeff.financing.service

import com.jeff.financing.dto.{CreateFlowCommand, FlowItem}
import com.jeff.financing.entity.Flow
import com.jeff.financing.enums.Category
import com.jeff.financing.repository.FlowRepository
import com.jeff.financing.repository.PersistenceImplicits._
import org.joda.time.{DateTime, Days}

import scala.concurrent.Future
import scala.math.BigDecimal.RoundingMode

trait FlowService extends DataConverter[Flow, FlowItem] {

  def list(): Future[Vector[FlowItem]] = {
    val future: Future[Vector[Flow]] = FlowRepository.list()
    super.convert2Vector(future, converter(_))
  }

  def save(command: CreateFlowCommand) = {
    val flow = Flow(None, command.platform, Category.withName(command.category), command.state.toInt, command.amount,
      command.rate, command.target, command.startTime.map(e => new DateTime(e).getMillis),
      command.endTime.map(e => new DateTime(e).getMillis), System.currentTimeMillis())
    FlowRepository.create(flow)
  }

  def save(flow: Flow) = {
    FlowRepository.create(flow)
  }

  def get(id: String): Future[Option[FlowItem]] = {
    val future = FlowRepository.get(id)
    super.convert2Obj(future, converter(_))
  }

  private def converter(flow: Flow): FlowItem = {
    val stateStr = if (flow.state == 1) "存入" else "取出"

    // 计算日收益和总收益
    val dailyDaysALlIncome = flow.rate map { rate =>
      // 计算总收益
      val daysALlIncome = flow.startTime.map { startTime =>
        // 经过的天数
        val days = Days.daysBetween(new DateTime(startTime), DateTime.now()).getDays
        (Some(days), Some((flow.amount * rate / 100 / 365 * days).setScale(4, RoundingMode.HALF_UP)))
      }
      // 日收益天数和总收益
      (Some((flow.amount * rate / 100 / 365).setScale(4, RoundingMode.HALF_UP)), daysALlIncome.flatMap(e => e._1), daysALlIncome.flatMap(e => e._2))
    }

    FlowItem(flow._id.get.stringify, flow.platform, Category.getDesc(flow.category), stateStr, flow.amount, flow.rate,
      dailyDaysALlIncome.flatMap(e => e._1), dailyDaysALlIncome.flatMap(e => e._2), dailyDaysALlIncome.flatMap(e => e._3),
      flow.target, flow.startTime.map(t => new DateTime(t).toString("yyyy-MM-dd")),
      flow.endTime.map(t => new DateTime(t).toString("yyyy-MM-dd")),
      new DateTime(flow.createTime).toString("yyyy-MM-dd"))
  }

}
