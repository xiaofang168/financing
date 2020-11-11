package com.jeff.financing.service

import com.jeff.financing.dto.CreateStocktakingCommand
import com.jeff.financing.entity.Stocktaking
import com.jeff.financing.repository.MongoExecutor
import com.jeff.financing.repository.PersistenceImplicits.stocktakingWriter
import org.joda.time.DateTime

trait StocktakingService extends MongoExecutor[Stocktaking] {

  def save(command: CreateStocktakingCommand) = {
    val date = DateTime.parse(command.date).getMillis
    val stocktaking = Stocktaking(None, command.targetId, date, command.amount, command.comment, System.currentTimeMillis)
    create(stocktaking)
  }

  //def list(targetId: String) = {
  // list(0, Int.MaxValue, document("targetId" -> targetId), document("createTime" -> -1))
  // }

}
