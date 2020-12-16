package com.jeff.financing.service

import com.jeff.financing.entity.{Flow, MonthlyReport, Stocktaking}
import com.jeff.financing.repository.MongoExecutor
import com.jeff.financing.repository.PersistenceImplicits._
import reactivemongo.api.bson.document

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MonthlyReportService extends MongoExecutor[MonthlyReport] {

  val flowService = new FlowService {}
  val stocktakingService: StocktakingService = new StocktakingService {}

  def gen(date: Int): Unit = {
    // 查询是否生成
    val f: Future[Option[MonthlyReport]] = findOne(document("date" -> date))
    f flatMap {
      case Some(_) => Future("已经存在")
      case None => {
        for {
          flows <- findAllFlow()
          stocktaking <- findAllStocktaking(date)
        } yield {
          // 对流水和盘点进行统计

        }

      }
    }
  }

  /**
   * 查询所有流水
   */
  def findAllFlow(): Future[List[Flow]] = {
    // 查询所有流水记录
    val f = flowService.list(0, Int.MaxValue, document("state" -> 1), document("_id" -> -1))
    f map (_.toList)
  }

  /**
   * 按月份查询盘点
   */
  def findAllStocktaking(date: Int): Future[List[Stocktaking]] = {
    // 查询所有流水记录
    val f: Future[Vector[Stocktaking]] = stocktakingService.list(0, Int.MaxValue, document("date" -> date), document("_id" -> -1))
    f map (_.toList)
  }

}
