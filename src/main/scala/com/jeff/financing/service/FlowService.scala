package com.jeff.financing.service

import com.jeff.financing.dto.{CreateFlowCommand, FlowItem}
import com.jeff.financing.entity.{Flow, Stocktaking}
import com.jeff.financing.enums.{CategoryEnum, PlatformEnum}
import com.jeff.financing.repository.PersistenceImplicits._
import com.jeff.financing.repository.{FlowRepository, MongoExecutor}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Days}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.math.BigDecimal.RoundingMode

trait FlowService extends MongoExecutor[Flow] with DataConverter[Flow, FlowItem] {

  val stocktakingService = new StocktakingService {}

  def list(): Future[Vector[FlowItem]] = {
    val future: Future[Vector[Flow]] = FlowRepository.list()
    super.convert2VectorWithFuture(future, handle)
  }

  def list(startDate: Option[String], endDate: Option[String], platform: Option[String], category: Option[String]): Future[Vector[FlowItem]] = {
    val future: Future[Vector[Flow]] = FlowRepository.list(startDate, endDate, platform, category)
    super.convert2VectorWithFuture(future, handle)
  }

  def save(command: CreateFlowCommand) = {
    val flow = Flow(None, PlatformEnum.withName(command.platform), CategoryEnum.withName(command.category), 1, command.amount,
      command.rate, command.target, command.startDate.replaceAll("-", "").toInt,
      command.endDate.map(e => e.replaceAll("-", "").toInt))
    FlowRepository.create(flow)
  }

  def update(id: String, command: CreateFlowCommand): Future[Int] = {
    val obj = FlowRepository.get(id)
    for {
      result <- obj
      out <- {
        if (result.isEmpty) {
          Future(0)
        } else {
          val obj = result.get
          val flow = Flow(obj._id, PlatformEnum.withName(command.platform), CategoryEnum.withName(command.category), obj.state, command.amount,
            command.rate, command.target, command.startDate.replaceAll("-", "").toInt,
            command.endDate.map(e => e.replaceAll("-", "").toInt), obj.income, obj.createTime)
          super.update(id, flow)
        }
      }
    } yield {
      out
    }
  }

  def save(flow: Flow) = {
    FlowRepository.create(flow)
  }

  def delById(id: String): Future[Int] = {
    super.delete(id)
  }

  def get(id: String): Future[Option[FlowItem]] = {
    val future = FlowRepository.get(id)
    super.convert2ObjWithFuture(future, handle)
  }

  val handle: Flow => Future[FlowItem] = flow => {
    val f: Future[Option[Stocktaking]] = stocktakingService.findOne(flow._id.get.stringify)
    val fi: Future[FlowItem] = f.map(r => converter(flow, r))
    fi
  }

  private def converter(flow: Flow, stocktaking: Option[Stocktaking]): FlowItem = {
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
      flow.target,
      flow.startDate,
      flow.endDate,
      new DateTime(flow.createTime).toString("yyyy-MM-dd"))
  }

}
