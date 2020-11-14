package com.jeff.financing.service

import com.jeff.financing.dto.CreateStocktakingCommand
import com.jeff.financing.entity.Stocktaking
import com.jeff.financing.repository.MongoExecutor
import com.jeff.financing.repository.PersistenceImplicits.{stocktakingWriter, _}
import org.joda.time.DateTime
import reactivemongo.api.bson.document

trait StocktakingService extends MongoExecutor[Stocktaking] {

  def save(command: CreateStocktakingCommand) = {
    val date = DateTime.parse(command.date).getMillis
    val stocktaking = Stocktaking(None, command.targetId, date, command.amount, command.comment, System.currentTimeMillis)
    create(stocktaking)
  }

  def find(targetId: String) = {
    list(0, Int.MaxValue, document("targetId" -> targetId), document("createTime" -> -1))
  }

}
