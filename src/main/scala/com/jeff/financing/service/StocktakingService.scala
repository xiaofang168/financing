package com.jeff.financing.service

import com.jeff.financing.dto.{CreateStocktakingCommand, StocktakingItem}
import com.jeff.financing.entity.Stocktaking
import com.jeff.financing.repository.MongoExecutor
import com.jeff.financing.repository.PersistenceImplicits.{stocktakingWriter, _}
import org.joda.time.DateTime
import reactivemongo.api.bson.document

import scala.concurrent.Future

trait StocktakingService extends MongoExecutor[Stocktaking] with DataConverter[Stocktaking, StocktakingItem] {

  def save(command: CreateStocktakingCommand): Future[Boolean] = {
    val date = DateTime.parse(command.date).getMillis
    val stocktaking = Stocktaking(None, command.targetId, date, command.amount, command.comment, System.currentTimeMillis)
    create(stocktaking)
  }

  def find(): Future[Vector[StocktakingItem]] = {
    val future = list(0, Int.MaxValue, document("date" -> -1))
    super.convert2Vector(future, this.convert)
  }

  def find(targetId: String): Future[Vector[StocktakingItem]] = {
    val future = list(0, Int.MaxValue, document("targetId" -> targetId), document("date" -> -1))
    super.convert2Vector(future, this.convert)
  }

  def getById(id: String) = {
    super.convert2Obj(super.get(id), convert)
  }

  val convert: Stocktaking => StocktakingItem = stocktaking => {
    val date = new DateTime(stocktaking.date).toString("yyyy-MM-dd")
    StocktakingItem(stocktaking.targetId, stocktaking._id.get.stringify, date, stocktaking.amount, stocktaking.comment)
  }

}
