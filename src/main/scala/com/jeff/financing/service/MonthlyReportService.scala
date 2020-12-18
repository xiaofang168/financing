package com.jeff.financing.service


import com.jeff.financing.entity.{Flow, MonthlyReport, Stocktaking}
import com.jeff.financing.enums.CategoryEnum
import com.jeff.financing.enums.CategoryEnum.Category
import com.jeff.financing.repository.MongoExecutor
import com.jeff.financing.repository.PersistenceImplicits._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import reactivemongo.api.bson.document

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MonthlyReportService extends MongoExecutor[MonthlyReport] {

  val flowService = new FlowService {}
  val stocktakingService: StocktakingService = new StocktakingService {}

  def gen(date: Int): Unit = {
    // 上个月的时间
    val lastDate: Int = DateTime.parse(date.toString, DateTimeFormat.forPattern("yyyyMM"))
      .minusMonths(1)
      .toString("yyyyMM")
      .toInt
    // 查询是否生成
    val f: Future[Option[MonthlyReport]] = findOne(document("date" -> date))
    f flatMap {
      case Some(_) => Future("已经存在")
      case None => {
        for {
          flows <- findAllFlow()
          stocktaking <- findAllStocktaking(date)
          lastStocktaking <- findAllStocktaking(lastDate)
        } yield {
          // 根据流水计算初始化的本金

          // 根据盘点技术本息和

          // 对流水和盘点进行统计
          val flowMap: Map[Category, List[Flow]] = flows.groupBy(e => e.category)
          flowMap map {
            case (CategoryEnum.STOCK, v) =>
          }
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
