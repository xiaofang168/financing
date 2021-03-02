package com.jeff.financing.service

import cats.data.OptionT
import com.jeff.financing.dto.{CreateStocktakingCommand, StocktakingItem, StocktakingStats}
import com.jeff.financing.entity.Stocktaking
import com.jeff.financing.repository.MongoExecutor
import com.jeff.financing.repository.PersistenceImplicits.{stocktakingReader, stocktakingWriter}
import com.jeff.financing.str2Int
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import reactivemongo.api.Cursor
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONString, document}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.ClassTag

trait StocktakingService extends MongoExecutor[Stocktaking] with DataConverter[Stocktaking, StocktakingItem] {

  def save(command: CreateStocktakingCommand): Future[Boolean] = {
    // 时间转换为int
    import io.scalaland.chimney.dsl._
    val stocktaking = command.into[Stocktaking]
      .withFieldConst(_._id, None)
      .withFieldComputed(_.date, _ => str2Int(command.date))
      .withFieldComputed(_.createTime, _ => System.currentTimeMillis)
      .transform
    create(stocktaking)
  }

  def update(id: String, command: CreateStocktakingCommand): Future[Int] = {
    val obj = get(id)
    for {
      result <- obj
      out <- {
        if (result.isEmpty) {
          Future(0)
        } else {
          val obj = result.get
          val date = str2Int(command.date)
          val u = Stocktaking(obj._id, obj.targetId, date, command.amount, command.income, command.totalIncome, command.rate, command.comment, obj.createTime)
          super.update(id, u)
        }
      }
    } yield {
      out
    }
  }

  def find(): Future[Vector[StocktakingItem]] = {
    val future = list(0, Int.MaxValue, document("date" -> -1))
    super.convert2Vector(future, this.convert)
  }

  def findOne(targetId: String): OptionT[Future, Stocktaking] = {
    val a = findOne(document("targetId" -> targetId), document("date" -> -1))
    OptionT(a)
  }

  def find(targetId: String): Future[Vector[StocktakingItem]] = {
    val future = list(0, Int.MaxValue, document("targetId" -> targetId), document("date" -> -1))
    super.convert2Vector(future, this.convert)
  }

  /**
   * 统计资产最新的盘点记录
   *
   * @param mat
   * @param m
   * @param tag
   * @return
   */
  def aggregate(mat: BSONDocument)(implicit m: BSONDocumentReader[StocktakingStats], tag: ClassTag[Stocktaking]) = {
    exec(coll => {
      import coll.AggregationFramework.{Descending, FirstField, Group, Match, Project, Sort}
      val pipeline = List(Match(mat),
        Sort(Descending("date")),
        Project(document("date" -> 1, "amount" -> 1, "targetId" -> 1, "createTime" -> 1)),
        Group(BSONString("$targetId"))(
          "date" -> FirstField("date"),
          "amount" -> FirstField("amount"),
          "createTime" -> FirstField("createTime")))
      coll.aggregatorContext[StocktakingStats](pipeline)
        .prepared
        .cursor
        .collect[Vector](Int.MaxValue, Cursor.FailOnError[Vector[StocktakingStats]]())
    })
  }

  def getById(id: String) = {
    super.convert2Obj(super.get(id), convert)
  }

  def delById(id: String) = {
    super.delete(id)
  }

  val convert: Stocktaking => StocktakingItem = stocktaking => {
    val dateFormat = DateTime.parse(stocktaking.date.toString, DateTimeFormat.forPattern("yyyyMM")).toString("yyyy-MM")
    StocktakingItem(stocktaking.targetId, stocktaking._id.get.stringify, dateFormat, stocktaking.amount, stocktaking.income, stocktaking.totalIncome, stocktaking.rate, new DateTime(stocktaking.createTime).toString("yyyy-MM-dd"), stocktaking.comment)
  }

}
