package com.jeff.financing.service

import cats.implicits._
import com.jeff.financing.dto.{AssetReport, IncomeReport, MonthlyReportItem, MonthlyReportRow}
import com.jeff.financing.entity.{Flow, MonthlyReport, Stocktaking}
import com.jeff.financing.enums.CategoryEnum
import com.jeff.financing.repository.PersistenceImplicits._
import com.jeff.financing.repository.ZioMongoExecutor
import com.jeff.financing.service.ZFlow.ZFlowEnv
import com.jeff.financing.vo.{Asset, Capital, CapitalInterest, Income}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Months}
import reactivemongo.api.bson.{BSONDocument, document}
import zio.{Has, Task, ZIO, ZLayer}

import scala.collection.immutable.ListMap
import scala.language.postfixOps

object ZMonthlyReport {

  type ZMonthlyReportEnv = Has[ZMonthlyReport.Service]

  class Service(flowService: ZFlow.Service) extends ZioMongoExecutor[MonthlyReport] {
    def detail(date: Int): Task[List[MonthlyReportRow]] = {
      for {
        r <- super.findOne(document("date" -> date))
      } yield {
        if (r.isEmpty) {
          throw new RuntimeException(s"$date 详情不存在")
        }
        convert(r.get)
      }
    }

    val convert: MonthlyReport => List[MonthlyReportRow] = monthlyReport => {
      List(MonthlyReportRow("日期", Some(monthlyReport.date.toString), "基本信息"),
        MonthlyReportRow("本金和", Some(monthlyReport.capitalSum.toString()), "基本信息"),
        MonthlyReportRow("本息和", Some(monthlyReport.capitalInterestSum.toString()), "基本信息"),
        MonthlyReportRow("总收益", Some(monthlyReport.incomeSum.toString()), "基本信息"),
        MonthlyReportRow("创建时间", Some(new DateTime(monthlyReport.createTime).toString("yyyy-MM-dd")), "基本信息"),
        MonthlyReportRow("股票", monthlyReport.income.stock.map(e => e.toString()), "月收益"),
        MonthlyReportRow("股票基金", monthlyReport.income.stockFund.map(e => e.toString()), "月收益"),
        MonthlyReportRow("指数基金", monthlyReport.income.indexFund.map(e => e.toString()), "月收益"),
        MonthlyReportRow("债券基金", monthlyReport.income.bondFund.map(e => e.toString()), "月收益"),
        MonthlyReportRow("货币基金", monthlyReport.income.monetaryFund.map(e => e.toString()), "月收益"),
        MonthlyReportRow("保险", monthlyReport.income.insurance.map(e => e.toString()), "月收益"),
        MonthlyReportRow("银行理财", monthlyReport.income.bank.map(e => e.toString()), "月收益"),
        MonthlyReportRow("储蓄", monthlyReport.income.saving.map(e => e.toString()), "月收益"),
        MonthlyReportRow("股票", monthlyReport.capital.stock.map(e => e.toString()), "本金"),
        MonthlyReportRow("股票基金", monthlyReport.capital.stockFund.map(e => e.toString()), "本金"),
        MonthlyReportRow("指数基金", monthlyReport.capital.indexFund.map(e => e.toString()), "本金"),
        MonthlyReportRow("债券基金", monthlyReport.capital.bondFund.map(e => e.toString()), "本金"),
        MonthlyReportRow("货币基金", monthlyReport.capital.monetaryFund.map(e => e.toString()), "本金"),
        MonthlyReportRow("保险", monthlyReport.capital.insurance.map(e => e.toString()), "本金"),
        MonthlyReportRow("银行理财", monthlyReport.capital.bank.map(e => e.toString()), "本金"),
        MonthlyReportRow("储蓄", monthlyReport.capital.saving.map(e => e.toString()), "本金"),
        MonthlyReportRow("股票", monthlyReport.capitalInterest.stock.map(e => e.toString()), "本息"),
        MonthlyReportRow("股票基金", monthlyReport.capitalInterest.stockFund.map(e => e.toString()), "本息"),
        MonthlyReportRow("指数基金", monthlyReport.capitalInterest.indexFund.map(e => e.toString()), "本息"),
        MonthlyReportRow("债券基金", monthlyReport.capitalInterest.bondFund.map(e => e.toString()), "本息"),
        MonthlyReportRow("货币基金", monthlyReport.capitalInterest.monetaryFund.map(e => e.toString()), "本息"),
        MonthlyReportRow("保险", monthlyReport.capitalInterest.insurance.map(e => e.toString()), "本息"),
        MonthlyReportRow("银行理财", monthlyReport.capitalInterest.bank.map(e => e.toString()), "本息"),
        MonthlyReportRow("储蓄", monthlyReport.capitalInterest.saving.map(e => e.toString()), "本息"))
    }

    /**
     * 生成月报
     *
     * @param date
     */
    def gen(date: Int): Task[Boolean] = {
      // 查询是否生成
      val task: Task[Option[MonthlyReport]] = findOne(document("date" -> date))
      for {
        d <- task
        monthlyReport <- {
          if (d.isDefined) {
            throw new RuntimeException("已经生成过报表")
          }
          // 并行计算
          val a = findAllFlow()
          val b = findAllStocktaking(date)
          for {
            flows <- a
            stocktakingList <- b
          } yield {
            // 计算本金和
            val capitalSum: BigDecimal = flows.map(_.amount).sum

            // 已经盘点的资金和
            val hadStocktakingCapitalInterestSum = stocktakingList.map(_.amount).sum

            // 未进行当月盘点的资金
            val targetIds = stocktakingList.map(_.targetId)
            val noStocktakingCapitalInterestSum = flows.filter(e => !targetIds.contains(e._id.get.stringify)).map(e => e.amount).sum

            // 计算本息和
            val capitalInterestSum = hadStocktakingCapitalInterestSum + noStocktakingCapitalInterestSum

            // 分类本金组
            val categoryFlowAmountCountMap: Map[String, BigDecimal] = flows.groupBy(_.category.toString)
                                                                           .view
                                                                           .mapValues(_.map(_.amount).sum)
                                                                           .toMap
            // 类别对应的资产id
            val categoryFlowIdsMap: Map[String, List[String]] = flows.groupBy(_.category.toString).view.mapValues(_.map(_._id.get.stringify)).toMap

            // 按照类别对应的标的id进行统计(类别->盘点金额和&盘点收益和)
            val clCFSAmountMap: Map[String, (BigDecimal, BigDecimal)] = categoryFlowIdsMap.map { e =>
              // 当前类别盘点过标的集合
              val stockingTarget: List[Stocktaking] = stocktakingList.filter(s => e._2.contains(s.targetId))

              // 当前类别没有盘点过的标的id集合
              val notStockingTargetIds: List[String] = e._2.diff(stockingTarget.map(_.targetId))

              // 当前月标的对应的盘点总金额和收益总金额
              val stockingSums: (BigDecimal, BigDecimal) = stockingTarget
                .foldLeft(BigDecimal(0), BigDecimal(0))((x, item) => (x._1 + item.amount, x._2 + item.income))

              // 当前月标的未盘点过的总金额和收益总金额
              val notStockingSums: (BigDecimal, BigDecimal) = flows
                .filter(e => notStockingTargetIds.contains(e._id.get.stringify))
                .foldLeft(BigDecimal(0), BigDecimal(0))((x, item) => (x._1 + item.amount, x._2 + 0))

              // 当月盘点过和未盘点过的总金额(盘点金额和收益)
              val sums = stockingSums |+| notStockingSums

              // 当月类别对应的盘点
              e._1 -> (sums._1, sums._2)
            }

            // 计算盘点的收益和
            val incomeSum: BigDecimal = clCFSAmountMap.values.map(e => e._2).sum

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

            MonthlyReport(None, date, capitalSum, capitalInterestSum, incomeSum, capital, capitalInterest, income)
          }
        }
        r <- create(monthlyReport)
      } yield r
    }

    def findIncome(startDate: Int, endDate: Int): Task[IncomeReport] = {
      val task = find(startDate, endDate, document("date" -> 1))
      for {
        r <- task
      } yield {
        val value = r.map(e => Array(e.date.toString, e.capitalSum, e.capitalInterestSum, e.incomeSum))
        val transpose = value.toArray.transpose
        val dates: List[String] = transpose(0) map (_.toString) toList
        val capitals: List[BigDecimal] = transpose(1) map (_.asInstanceOf[BigDecimal]) toList
        val capitalInterests: List[BigDecimal] = transpose(2) map (_.asInstanceOf[BigDecimal]) toList
        val incomes: List[BigDecimal] = transpose(3) map (_.asInstanceOf[BigDecimal]) toList;
        IncomeReport(dates, capitals, capitalInterests, incomes)
      }
    }

    def findAssert(startDate: Int, endDate: Int, typ: Int): Task[AssetReport] = {
      val task = find(startDate, endDate, document("date" -> 1))
      for {
        r <- task
      } yield {
        val value = if (typ == 0) r.map(e => Array(e.date.toString, e.capital)) else r.map(e => Array(e.date.toString, e.capitalInterest))
        val transpose = value.toArray.transpose
        val dates: List[String] = transpose(0) map (_.toString) toList;
        convert(dates, transpose(1) map (_.asInstanceOf[Asset]) toList)
      }
    }

    private def convert(dates: List[String], capitals: List[Asset]): AssetReport = {
      val stocks: List[BigDecimal] = capitals.map(e => e.stock.getOrElse(BigDecimal(0)))
      val stockFunds: List[BigDecimal] = capitals.map(e => e.stockFund.getOrElse(BigDecimal(0)))
      val indexFunds: List[BigDecimal] = capitals.map(e => e.indexFund.getOrElse(BigDecimal(0)))
      val bondFunds: List[BigDecimal] = capitals.map(e => e.bondFund.getOrElse(BigDecimal(0)))
      val monetaryFunds: List[BigDecimal] = capitals.map(e => e.monetaryFund.getOrElse(BigDecimal(0)))
      val insurances: List[BigDecimal] = capitals.map(e => e.insurance.getOrElse(BigDecimal(0)))
      val banks: List[BigDecimal] = capitals.map(e => e.bank.getOrElse(BigDecimal(0)))
      val savings: List[BigDecimal] = capitals.map(e => e.saving.getOrElse(BigDecimal(0)))
      AssetReport(dates, stocks, stockFunds, indexFunds, bondFunds, monetaryFunds, insurances, banks, savings)
    }

    private def find(startDate: Int, endDate: Int, sortDoc: BSONDocument = document("date" -> -1)): Task[Vector[MonthlyReport]] = {
      list(0, Int.MaxValue, document("date" -> document("$gte" -> startDate, "$lte" -> endDate)), sortDoc)
    }

    /**
     * 查询所有流水
     */
    private def findAllFlow(): Task[List[Flow]] = {
      // 查询所有流水记录
      val f = flowService.list(0, Int.MaxValue, document("state" -> 1), document("_id" -> -1))
      f map (_.toList)
    }

    /**
     * 按月份查询盘点
     */
    private def findAllStocktaking(date: Int): Task[List[Stocktaking]] = {
      // 查询所有流水记录
      val f: Task[Vector[Stocktaking]] = flowService.stocktakingService.list(0, Int.MaxValue, document("date" -> date), document("_id" -> -1))
      f map (_.toList)
    }

    def previews(startDate: Int, endDate: Int): Task[List[MonthlyReportItem]] = {
      val starDateTime = DateTime.parse(startDate.toString, DateTimeFormat.forPattern("yyyyMM"))
      val endDateTime = DateTime.parse(endDate.toString, DateTimeFormat.forPattern("yyyyMM"))
      val m = Months.monthsBetween(starDateTime, endDateTime)
      val task: Task[Vector[MonthlyReport]] = find(startDate, endDate)
      for {
        r <- task
      } yield {
        val monthMap = getMonthMap(starDateTime.getMillis, m.getMonths)
        val map: Map[Int, MonthlyReport] = r.map(t => t.date -> t).toMap
        monthMap.map {
          case (k, v) => {
            if (map.contains(k)) {
              val mr = map.get(k).get
              MonthlyReportItem(Some(mr._id.get.stringify), k, v)
            } else {
              MonthlyReportItem(None, k, 0)
            }
          }
        }.toList
      }
    }

    private def getMonthMap(startTime: Long, months: Int): ListMap[Int, Int] = {
      val starDateTime = new DateTime(startTime)
      val range = months + 1 to 0 by -1
      range map { e =>
        starDateTime.plusMonths(e).toString("yyyyMM").toInt -> 1
      } to ListMap
    }
  }

  val live: ZLayer[ZFlowEnv, Nothing, ZMonthlyReportEnv] = ZLayer.fromService[ZFlow.Service, ZMonthlyReport.Service] { flowService =>
    new Service(flowService)
  }

  def previews(startDate: Int, endDate: Int): ZIO[ZMonthlyReportEnv, Throwable, List[MonthlyReportItem]] = ZIO.accessM(_.get.previews(startDate, endDate))

  def detail(date: Int): ZIO[ZMonthlyReportEnv, Throwable, List[MonthlyReportRow]] = ZIO.accessM(_.get.detail(date))

  def gen(date: Int): ZIO[ZMonthlyReportEnv, Throwable, Boolean] = ZIO.accessM(_.get.gen(date))

  def findIncome(startDate: Int, endDate: Int): ZIO[ZMonthlyReportEnv, Throwable, IncomeReport] = ZIO.accessM(_.get.findIncome(startDate, endDate))

  def findAssert(startDate: Int, endDate: Int, typ: Int): ZIO[ZMonthlyReportEnv, Throwable, AssetReport] = ZIO.accessM(_.get.findAssert(startDate, endDate, typ))

}
