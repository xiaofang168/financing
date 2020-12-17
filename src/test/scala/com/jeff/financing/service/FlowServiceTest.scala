package com.jeff.financing.service


import com.jeff.financing._
import com.jeff.financing.entity.{Flow, Stocktaking}
import com.jeff.financing.enums.Category
import org.junit.Test
import reactivemongo.api.bson.BSONObjectID

import scala.concurrent.ExecutionContext.Implicits.global
import scala.language.postfixOps
import scala.util.{Failure, Success}

class FlowServiceTest {

  @Test
  def list(): Unit = {
    val flows = List(Flow(None, Some("蚂蚁财富"), Category.STOCK_FUND, 1, 1000, Some(0.045F), "新华轮换混合", Some(20200306), Some(20200906)),
      Flow(None, Some("京东金融"), Category.SAVING, 1, 10000, Some(0.045F), "中关村银行", Some(20200306), Some(20200906)))

    val r = sort(flows, Seq(
      SortingField("amount", Ordering[Float].reverse),
      SortingField[Option[Float]]("rate", Ordering[Option[Float]].reverse)
    ))

    r.foreach(println(_))

  }

  @Test
  def save(): Unit = {
    val flowService = new FlowService {}
    val flow = Flow(None, Some("蚂蚁财富"), Category.STOCK_FUND, 1, 1000, Some(0.045),
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
    val flows = List(Flow(Some(BSONObjectID.parse("5fc77d278f3c0f3ee0924e2d").get), Some("蚂蚁财富"), Category.STOCK_FUND, 1, 18000, Some(0.045F),
      "新华轮换混合", Some(20200306), Some(20200906)),
      Flow(Some(BSONObjectID.parse("5fc77e4c8f3c0f3ee0924e73").get), Some("京东金融"), Category.SAVING, 1, 28000, Some(0.045F),
        "中关村银行", Some(20200306), Some(20200906)),
      Flow(Some(BSONObjectID.parse("5fc77e818f3c0f3ee0924e84").get), Some("京东金融"), Category.SAVING, 1, 10000, Some(0.045F),
        "中关村银行", Some(20200306), Some(20200906))
    )

    val stocktaking = List(Stocktaking(None, "5fc77d278f3c0f3ee0924e2d", 202012, BigDecimal(20000)),
      Stocktaking(None, "5fc77e4c8f3c0f3ee0924e73", 202012, BigDecimal(30000)))

    val lastStocktaking = List(Stocktaking(None, "5fc77d278f3c0f3ee0924e2d", 202011, BigDecimal(18000)),
      Stocktaking(None, "5fc77e4c8f3c0f3ee0924e73", 202011, BigDecimal(28000))
    )


    // 分类本金和
    val flowAmountCountMap: Map[String, BigDecimal] = flows.groupBy(e => e.category.toString)
      .view
      .mapValues(_.map(_.amount).sum)
      .toMap


    println(flowAmountCountMap)
  }

}
