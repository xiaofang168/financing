package com.jeff.financing.service


import com.jeff.financing.dto.{AssetReport, IncomeReport, MonthlyReportItem, MonthlyReportRow}
import com.jeff.financing.entity.{Flow, MonthlyReport, Stocktaking}
import com.jeff.financing.enums.CategoryEnum
import com.jeff.financing.repository.MongoExecutor
import com.jeff.financing.repository.PersistenceImplicits._
import com.jeff.financing.vo.{Asset, Capital, CapitalInterest, Income}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, Months}
import reactivemongo.api.bson.{BSONDocument, document}

import scala.collection.immutable
import scala.collection.immutable.ListMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

trait MonthlyReportService extends MongoExecutor[MonthlyReport] {

  val flowService = new FlowService {}
  val stocktakingService: StocktakingService = new StocktakingService {}

  def detail(date: Int): Future[Option[List[MonthlyReportRow]]] = {
    val a = new DataConverter[MonthlyReport, List[MonthlyReportRow]] {}
    a.convert2Obj(super.findOne(document("date" -> date)), convert)
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
  def gen(date: Int): Future[Boolean] = {
    // 上个月的时间
    val lastDate: Int = DateTime.parse(date.toString, DateTimeFormat.forPattern("yyyyMM"))
      .minusMonths(1)
      .toString("yyyyMM")
      .toInt
    // 查询是否生成
    val f: Future[Option[MonthlyReport]] = findOne(document("date" -> date))
    val a: Future[Future[Boolean]] = f flatMap {
      case Some(_) => Future(Future(false))
      case None => {
        // 并行计算
        val a = findAllFlow()
        val b = findAllStocktaking(date)
        val c = findAllStocktaking(lastDate)
        for {
          flows <- a
          stocktaking <- b
          lastStocktaking <- c
        } yield {
          // 计算本金和
          val capitalSum: BigDecimal = flows.map(_.amount).sum

          // 已经盘点的资金和
          val hadStocktakingCapitalInterestSum = stocktaking.map(_.amount).sum
          val targetIds = stocktaking.map(_.targetId)
          // 未进行当月盘点的资金
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
          // 按照类别对应的标的id进行统计
          val clCFSAmountMap: Map[String, (BigDecimal, BigDecimal)] = categoryFlowIdsMap.map { e =>
            // 当前月标的对应的盘点总金额和收益总金额
            val sums: (BigDecimal, BigDecimal) = stocktaking
              .filter(s => e._2.contains(s.targetId))
              .foldLeft(BigDecimal(0), BigDecimal(0))((x, item) => (x._1 + item.amount, x._2 + item.income))
            // 上月未盘点过,直接取填的收益
            val b = if (lastStocktaking.size == 0) {
              sums._2
            } else {
              // 上个月标的对应的盘点总金额
              sums._1 - lastStocktaking.filter(s => e._2.contains(s.targetId)).map(_.amount).sum
            }
            // 类别对应的盘点收益
            e._1 -> (sums._1, b)
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

          val monthlyReport = MonthlyReport(None, date, capitalSum, capitalInterestSum, capitalInterestSum - capitalSum, capital, capitalInterest, income)

          // 生成月报
          create(monthlyReport)
        }
      }
    }
    a.flatten
  }

  def findIncome(startDate: Int, endDate: Int): Future[IncomeReport] = {
    val future = find(startDate, endDate, document("date" -> 1))
    for {
      f <- future
    } yield {
      val value = f.map(e => Array(e.date.toString, e.capitalSum, e.capitalInterestSum, e.incomeSum))
      val transpose = value.toArray.transpose
      val dates: List[String] = transpose(0) map (_.toString) toList
      val capitals: List[BigDecimal] = transpose(1) map (_.asInstanceOf[BigDecimal]) toList
      val capitalInterests: List[BigDecimal] = transpose(2) map (_.asInstanceOf[BigDecimal]) toList
      val incomes: List[BigDecimal] = transpose(3) map (_.asInstanceOf[BigDecimal]) toList;
      IncomeReport(dates, capitals, capitalInterests, incomes)
    }
  }

  def findAssert(startDate: Int, endDate: Int, typ: Int): Future[AssetReport] = {
    val future = find(startDate, endDate, document("date" -> 1))
    for {
      f <- future
    } yield {
      val value = if (typ == 0) f.map(e => Array(e.date.toString, e.capital)) else f.map(e => Array(e.date.toString, e.capitalInterest))
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

  private def find(startDate: Int, endDate: Int, sortDoc: BSONDocument = document("date" -> -1)): Future[Vector[MonthlyReport]] = {
    list(0, Int.MaxValue, document("date" -> document("$gte" -> startDate, "$lte" -> endDate)), sortDoc)
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

  def previews(startDate: Int, endDate: Int): Future[immutable.Iterable[MonthlyReportItem]] = {
    val starDateTime = DateTime.parse(startDate.toString, DateTimeFormat.forPattern("yyyyMM"))
    val endDateTime = DateTime.parse(endDate.toString, DateTimeFormat.forPattern("yyyyMM"))
    val m = Months.monthsBetween(starDateTime, endDateTime)
    val futureMonthMap: Future[ListMap[Int, Int]] = Future {
      getMonthMap(starDateTime.getMillis, m.getMonths)
    }
    val future: Future[Vector[MonthlyReport]] = find(startDate, endDate)
    for {
      monthMap <- futureMonthMap
      r <- future
    } yield {
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
      }
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
