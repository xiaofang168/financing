package com.jeff.financing.service

import com.jeff.financing.dto.{CreateFlowCommand, FlowItem, StocktakingStats}
import com.jeff.financing.entity.Flow
import com.jeff.financing.enums.{CategoryEnum, PlatformEnum}
import com.jeff.financing.repository.PersistenceImplicits._
import com.jeff.financing.repository.{FlowRepository, ZioMongoExecutor}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Days}
import reactivemongo.api.bson.document
import zio.{Task, ZIO}

import scala.math.BigDecimal.RoundingMode

trait FlowService extends ZioMongoExecutor[Flow] {

  val stocktakingService = new StocktakingService {}

  def list(): Task[Vector[FlowItem]] = {
    val task: Task[Vector[Flow]] = FlowRepository.list()
    convert(task)
  }

  def list(startDate: Option[String], endDate: Option[String], platform: Option[String], category: Option[String]): Task[Vector[FlowItem]] = {
    val task: Task[Vector[Flow]] = FlowRepository.list(startDate, endDate, platform, category)
    convert(task)
  }

  private def convert(task: Task[Vector[Flow]]): Task[Vector[FlowItem]] = {
    for {
      r <- task
      a <- {
        val targetIds = r.map(e => e._id.get.stringify)
        stocktakingService.aggregate(document("targetId" -> document("$in" -> targetIds)))
      }
    } yield {
      handles(r, a)
    }
  }

  def save(command: CreateFlowCommand) = {
    val flow = Flow(None, PlatformEnum.withName(command.platform), CategoryEnum.withName(command.category), 1, command.amount,
      command.rate, command.target, command.startDate.replaceAll("-", "").toInt,
      command.endDate.map(e => e.replaceAll("-", "").toInt))
    FlowRepository.create(flow)
  }

  def update(id: String, command: CreateFlowCommand): ZIO[Any, Throwable, Int] = {
    val obj = FlowRepository.get(id)
    for {
      result <- obj
      out <- {
        if (result.isEmpty) {
          throw new RuntimeException("流水记录不存在")
        }
        val obj = result.get
        val flow = Flow(obj._id, PlatformEnum.withName(command.platform), CategoryEnum.withName(command.category), obj.state, command.amount,
          command.rate, command.target, command.startDate.replaceAll("-", "").toInt,
          command.endDate.map(e => e.replaceAll("-", "").toInt), obj.income, obj.createTime)
        super.update(id, flow)
      }
    } yield {
      out
    }
  }

  def save(flow: Flow) = {
    FlowRepository.create(flow)
  }

  def delById(id: String): Task[Int] = {
    super.delete(id)
  }

  def get(id: String): Task[FlowItem] = {
    val task = FlowRepository.get(id)
    for {
      r <- task
      a <- {
        if (r.isEmpty) {
          throw new RuntimeException("流水记录不存在")
        }
        stocktakingService.findOne(r.get._id.get.stringify)
      }
    } yield {
      a.map(e => converter(r.get, Some(StocktakingStats(e.targetId, e.date, e.amount, e.income, e.createTime)))).getOrElse(converter(r.get, None))
    }
  }

  val handles: (Vector[Flow], Vector[StocktakingStats]) => Vector[FlowItem] = (flows, stocktakingStats) => {
    val stocktakingStatsMap = stocktakingStats.map(e => (e._id, e)).toMap
    flows.map(flow => converter(flow, stocktakingStatsMap.get(flow._id.get.stringify)))
  }

  private def converter(flow: Flow, stocktaking: Option[StocktakingStats]): FlowItem = {
    val stateStr = if (flow.state == 1) "存入" else "取出"

    // 经过的天数
    val startTime = DateTime.parse(flow.startDate.toString, DateTimeFormat.forPattern("yyyyMMdd"))
    val days = Days.daysBetween(startTime, DateTime.now()).getDays

    // 计算日收益
    val dailyIncome: Option[BigDecimal] = flow.rate map (rate => (flow.amount * rate / 100 / 365).setScale(2, RoundingMode.HALF_UP))

    // 盘点日期和金额
    val stocktakingDateAmount: (String, BigDecimal) = stocktaking match {
      case Some(e) => (new DateTime(e.createTime).toString("yyyy-MM-dd"), e.amount)
      case None => ("未盘点过", flow.amount)
    }

    FlowItem(flow._id.get.stringify, flow.platform.toString, PlatformEnum.getDesc(flow.platform),
      flow.category.toString, CategoryEnum.getDesc(flow.category), flow.state,
      stateStr, flow.amount, flow.rate,
      dailyIncome,
      days,
      dailyIncome.map(e => e * days),
      stocktakingDateAmount._1,
      stocktakingDateAmount._2,
      stocktaking.map(e => e.income).getOrElse(0),
      flow.target,
      flow.startDate,
      flow.endDate,
      new DateTime(flow.createTime).toString("yyyy-MM-dd"))
  }

}
