package com.jeff.financing.service


import com.jeff.financing.dto.MonthlyReportItem
import com.jeff.financing.entity.{Flow, MonthlyReport, Stocktaking}
import com.jeff.financing.enums.CategoryEnum
import com.jeff.financing.repository.MongoExecutor
import com.jeff.financing.repository.PersistenceImplicits._
import com.jeff.financing.vo.{Capital, CapitalInterest, Income}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Months}
import reactivemongo.api.bson.document

import scala.collection.immutable.SortedMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait MonthlyReportService extends MongoExecutor[MonthlyReport] {

  val flowService = new FlowService {}
  val stocktakingService: StocktakingService = new StocktakingService {}

  /**
   * 生成月报
   *
   * @param date
   */
  def gen(date: Int): Future[MonthlyReport] = {
    // 上个月的时间
    val lastDate: Int = DateTime.parse(date.toString, DateTimeFormat.forPattern("yyyyMM"))
      .minusMonths(1)
      .toString("yyyyMM")
      .toInt
    // 查询是否生成
    val f: Future[Option[MonthlyReport]] = findOne(document("date" -> date))
    f flatMap {
      case Some(a@_) => Future(a)
      case None => {
        for {
          flows <- findAllFlow()
          stocktaking <- findAllStocktaking(date)
          lastStocktaking <- findAllStocktaking(lastDate)
        } yield {
          // 分类本金和
          val categoryFlowAmountCountMap: Map[String, BigDecimal] = flows.groupBy(e => e.category.toString)
            .view
            .mapValues(_.map(_.amount).sum)
            .toMap
          // 类别对应的资产id
          val categoryFlowIdsMap: Map[String, List[String]] = flows.groupBy(e => e.category.toString).view.mapValues(_.map(_._id.get.stringify)).toMap
          // 按照类别对应的标的id进行统计
          val clCFSAmountMap: Map[String, (BigDecimal, BigDecimal)] = categoryFlowIdsMap.map { e =>
            // 当前月标的对应的盘点总金额
            val a = stocktaking.filter(s => e._2.contains(s.targetId)).map(_.amount).sum
            // 上个月标的对应的盘点总金额
            val b = lastStocktaking.filter(s => e._2.contains(s.targetId)).map(_.amount).sum
            // 类别对应的盘点收益
            e._1 -> (a, a - b)
          }

          // 本金
          val capital = Capital(categoryFlowAmountCountMap.get(CategoryEnum.STOCK.toString),
            categoryFlowAmountCountMap.get(CategoryEnum.STOCK_FUND.toString),
            categoryFlowAmountCountMap.get(CategoryEnum.INDEX_FUND.toString),
            categoryFlowAmountCountMap.get(CategoryEnum.BOND_FUND.toString),
            categoryFlowAmountCountMap.get(CategoryEnum.MONETARY_FUND.toString),
            categoryFlowAmountCountMap.get(CategoryEnum.INSURANCE.toString),
            categoryFlowAmountCountMap.get(CategoryEnum.BANK.toString),
            categoryFlowAmountCountMap.get(CategoryEnum.SAVING.toString))

          // 本息
          val capitalInterest = CapitalInterest(clCFSAmountMap.get(CategoryEnum.STOCK.toString).map(_._1),
            clCFSAmountMap.get(CategoryEnum.STOCK_FUND.toString).map(_._1),
            clCFSAmountMap.get(CategoryEnum.INDEX_FUND.toString).map(_._1),
            clCFSAmountMap.get(CategoryEnum.BOND_FUND.toString).map(_._1),
            clCFSAmountMap.get(CategoryEnum.MONETARY_FUND.toString).map(_._1),
            clCFSAmountMap.get(CategoryEnum.INSURANCE.toString).map(_._1),
            clCFSAmountMap.get(CategoryEnum.BANK.toString).map(_._1),
            clCFSAmountMap.get(CategoryEnum.SAVING.toString).map(_._1))

          // 收益
          val income = Income(clCFSAmountMap.get(CategoryEnum.STOCK.toString).map(_._2),
            clCFSAmountMap.get(CategoryEnum.STOCK_FUND.toString).map(_._2),
            clCFSAmountMap.get(CategoryEnum.INDEX_FUND.toString).map(_._2),
            clCFSAmountMap.get(CategoryEnum.BOND_FUND.toString).map(_._2),
            clCFSAmountMap.get(CategoryEnum.MONETARY_FUND.toString).map(_._2),
            clCFSAmountMap.get(CategoryEnum.INSURANCE.toString).map(_._2),
            clCFSAmountMap.get(CategoryEnum.BANK.toString).map(_._2),
            clCFSAmountMap.get(CategoryEnum.SAVING.toString).map(_._2))

          MonthlyReport(None, date, capital, capitalInterest, income)
        }
      }
    }
  }

  /**
   * 查询所有流水
   */
  private def findAllFlow(): Future[List[Flow]] = {
    // 查询所有流水记录
    val f = flowService.list(0, Int.MaxValue, document("state" -> 1), document("_id" -> -1))
    f map (_.toList)
  }

  /**
   * 按月份查询盘点
   */
  private def findAllStocktaking(date: Int): Future[List[Stocktaking]] = {
    // 查询所有流水记录
    val f: Future[Vector[Stocktaking]] = stocktakingService.list(0, Int.MaxValue, document("date" -> date), document("_id" -> -1))
    f map (_.toList)
  }

  def previews(startDate: Int, endDate: Int): Future[Vector[MonthlyReportItem]] = {
    val starDateTime = DateTime.parse(startDate.toString, DateTimeFormat.forPattern("yyyyMM"))
    val endDateTime = DateTime.parse(endDate.toString, DateTimeFormat.forPattern("yyyyMM"))
    val m = Months.monthsBetween(starDateTime, endDateTime)
    val monthMap: Map[Int, Int] = getMonthMap(starDateTime.getMillis, m.getMonths)
    val future: Future[Vector[MonthlyReport]] = list(0, Int.MaxValue,
      document("date" -> document("$gte" -> startDate, "$lte" -> endDate)))
    for {
      r <- future
    } yield {
      r map { e =>
        val isGen = if (monthMap.contains(e.date))
          1
        else 0
        MonthlyReportItem(e._id.get.stringify, e.date, isGen)
      }
    }
  }

  private def getMonthMap(startTime: Long, months: Int): SortedMap[Int, Int] = {
    val starDateTime = new DateTime(startTime)
    val range = 0L to months + 1
    range map { e =>
      starDateTime.plusMonths(e.toInt).toString("yyyyMM").toInt -> 0
    } to SortedMap
  }

}
