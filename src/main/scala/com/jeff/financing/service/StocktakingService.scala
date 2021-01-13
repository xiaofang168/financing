package com.jeff.financing.service

import com.jeff.financing.dto.{CreateStocktakingCommand, StocktakingItem}
import com.jeff.financing.entity.Stocktaking
import com.jeff.financing.repository.MongoExecutor
import com.jeff.financing.repository.PersistenceImplicits.{stocktakingWriter, _}
import com.jeff.financing.str2Int
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import reactivemongo.api.bson.document

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait StocktakingService extends MongoExecutor[Stocktaking] with DataConverter[Stocktaking, StocktakingItem] {

  def save(command: CreateStocktakingCommand): Future[Boolean] = {
    // 时间转换为int
    val date = str2Int(command.date)
    val stocktaking = Stocktaking(None, command.targetId, date, command.amount, command.rate, command.comment, System.currentTimeMillis)
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
          val u = Stocktaking(obj._id, obj.targetId, date, command.amount, command.rate, command.comment, obj.createTime)
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

  def findOne(targetId: String): Future[Option[Stocktaking]] = {
    findOne(document("targetId" -> targetId), document("date" -> -1))
  }

  def find(targetId: String): Future[Vector[StocktakingItem]] = {
    val future = list(0, Int.MaxValue, document("targetId" -> targetId), document("date" -> -1))
    super.convert2Vector(future, this.convert)
  }

  def getById(id: String) = {
    super.convert2Obj(super.get(id), convert)
  }

  def delById(id: String) = {
    super.delete(id)
  }

  val convert: Stocktaking => StocktakingItem = stocktaking => {
    val dateFormat = DateTime.parse(stocktaking.date.toString, DateTimeFormat.forPattern("yyyyMM")).toString("yyyy-MM")
    StocktakingItem(stocktaking.targetId, stocktaking._id.get.stringify, dateFormat, stocktaking.amount, stocktaking.rate, new DateTime(stocktaking.createTime).toString("yyyy-MM-dd"), stocktaking.comment)
  }

}
