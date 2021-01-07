package com.jeff.financing.service


import com.jeff.financing._
import com.jeff.financing.entity.{Flow, Stocktaking}
import com.jeff.financing.enums.{CategoryEnum, PlatformEnum}
import org.junit.Test
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.util.{Failure, Success}

class FlowServiceTest {

  @Test
  def list(): Unit = {
    val flows = List(Flow(None, PlatformEnum.ALI, CategoryEnum.STOCK_FUND, 1, 1000, Some(0.045F), "新华轮换混合", Some(20200306), Some(20200906)),
      Flow(None, PlatformEnum.JD, CategoryEnum.SAVING, 1, 10000, Some(0.045F), "中关村银行", Some(20200306), Some(20200906)))

    val r = sort(flows, Seq(
      SortingField("amount", Ordering[Float].reverse),
      SortingField[Option[Float]]("rate", Ordering[Option[Float]].reverse)
    ))

    r.foreach(println(_))
  }

  @Test
  def save(): Unit = {
    val flowService = new FlowService {}
    val flow = Flow(None, PlatformEnum.ALI, CategoryEnum.STOCK_FUND, 1, 1000, Some(0.045),
      "新华轮换混合", Some(20200306), Some(20200906))
    val f = flowService.save(flow)
    f onComplete {
      case Success(value) => println(value)
      case Failure(exception) => exception.printStackTrace()
    }
    Thread.sleep(5000)
  }

  @Test
  def search(): Unit = {
    val flowService = new FlowService {}
    val f = flowService.list()
    f onComplete {
      case Success(value) => println(">>>" + value)
      case Failure(exception) => exception.printStackTrace()
    }
    Thread.sleep(5000)
  }

  @Test
  def group(): Unit = {
    val flows = List(Flow(Some(BSONObjectID.parse("5fc77d278f3c0f3ee0924e2d").get), PlatformEnum.ALI, CategoryEnum.STOCK_FUND, 1, 18000, Some(0.045F),
      "新华轮换混合", Some(20200306), Some(20200906)),
      Flow(Some(BSONObjectID.parse("5fc77e4c8f3c0f3ee0924e73").get), PlatformEnum.JD, CategoryEnum.SAVING, 1, 28000, Some(0.045F),
        "中关村银行", Some(20200306), Some(20200906)),
      Flow(Some(BSONObjectID.parse("5fc77e818f3c0f3ee0924e84").get), PlatformEnum.JD, CategoryEnum.SAVING, 1, 10000, Some(0.045F),
        "中关村银行", Some(20200306), Some(20200906))
    )

    // 当前盘点数据
    val stocktaking = List(Stocktaking(None, "5fc77d278f3c0f3ee0924e2d", 202012, BigDecimal(20000)),
      Stocktaking(None, "5fc77e4c8f3c0f3ee0924e73", 202012, BigDecimal(30000)))

    // 上一次盘点数据
    val lastStocktaking = List(Stocktaking(None, "5fc77d278f3c0f3ee0924e2d", 202011, BigDecimal(18000)),
      Stocktaking(None, "5fc77e4c8f3c0f3ee0924e73", 202011, BigDecimal(28000))
    )

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

    val capital = com.jeff.financing.vo.Capital(categoryFlowAmountCountMap.get(CategoryEnum.STOCK.toString),
      categoryFlowAmountCountMap.get(CategoryEnum.STOCK_FUND.toString),
      categoryFlowAmountCountMap.get(CategoryEnum.INDEX_FUND.toString),
      categoryFlowAmountCountMap.get(CategoryEnum.BOND_FUND.toString),
      categoryFlowAmountCountMap.get(CategoryEnum.MONETARY_FUND.toString),
      categoryFlowAmountCountMap.get(CategoryEnum.INSURANCE.toString),
      categoryFlowAmountCountMap.get(CategoryEnum.BANK.toString),
      categoryFlowAmountCountMap.get(CategoryEnum.SAVING.toString))

    val capitalInterest = com.jeff.financing.vo.CapitalInterest(clCFSAmountMap.get(CategoryEnum.STOCK.toString).map(_._1),
      clCFSAmountMap.get(CategoryEnum.STOCK_FUND.toString).map(_._1),
      clCFSAmountMap.get(CategoryEnum.INDEX_FUND.toString).map(_._1),
      clCFSAmountMap.get(CategoryEnum.BOND_FUND.toString).map(_._1),
      clCFSAmountMap.get(CategoryEnum.MONETARY_FUND.toString).map(_._1),
      clCFSAmountMap.get(CategoryEnum.INSURANCE.toString).map(_._1),
      clCFSAmountMap.get(CategoryEnum.BANK.toString).map(_._1),
      clCFSAmountMap.get(CategoryEnum.SAVING.toString).map(_._1))

    val income = com.jeff.financing.vo.Income(clCFSAmountMap.get(CategoryEnum.STOCK.toString).map(_._2),
      clCFSAmountMap.get(CategoryEnum.STOCK_FUND.toString).map(_._2),
      clCFSAmountMap.get(CategoryEnum.INDEX_FUND.toString).map(_._2),
      clCFSAmountMap.get(CategoryEnum.BOND_FUND.toString).map(_._2),
      clCFSAmountMap.get(CategoryEnum.MONETARY_FUND.toString).map(_._2),
      clCFSAmountMap.get(CategoryEnum.INSURANCE.toString).map(_._2),
      clCFSAmountMap.get(CategoryEnum.BANK.toString).map(_._2),
      clCFSAmountMap.get(CategoryEnum.SAVING.toString).map(_._2))

    val monthlyReport = com.jeff.financing.entity.MonthlyReport(None, 202012, 22, 222, 22, capital, capitalInterest, income)

    println(categoryFlowAmountCountMap)
    println(monthlyReport)
  }

}
