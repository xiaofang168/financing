package com.jeff.financing.service

import com.jeff.financing.dto.{CreateStocktakingCommand, StocktakingItem, StocktakingStats}
import com.jeff.financing.entity.Stocktaking
import com.jeff.financing.repository.PersistenceImplicits.{stocktakingReader, stocktakingWriter}
import com.jeff.financing.repository.ZioMongoExecutor
import com.jeff.financing.str2Int
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import reactivemongo.api.Cursor
import reactivemongo.api.bson.{BSONDocument, BSONDocumentReader, BSONString, document}
import zio.{Has, Task, ZIO, ZLayer}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.ClassTag

object ZStocktaking {

  type ZStocktakingEnv = Has[ZStocktaking.Service]

  class Service extends ZioMongoExecutor[Stocktaking] {
    def save(command: CreateStocktakingCommand): Task[Boolean] = {
      // 时间转换为int
      import io.scalaland.chimney.dsl._
      val stocktaking = command.into[Stocktaking]
                               .withFieldConst(_._id, None)
                               .withFieldComputed(_.date, _ => str2Int(command.date))
                               .withFieldComputed(_.createTime, _ => System.currentTimeMillis)
                               .transform
      create(stocktaking)
    }

    def update(id: String, command: CreateStocktakingCommand): Task[Int] = {
      val obj = get(id)
      for {
        result <- obj
        out <- result match {
          case None => Task.fail(new RuntimeException("未找到盘点记录"))
          case Some(obj) => {
            val date = str2Int(command.date)
            val u = Stocktaking(obj._id, obj.targetId, date, command.amount, command.income, command.totalIncome, command.rate, command.comment, obj.createTime)
            super.update(id, u)
          }
        }
      } yield out
    }

    def find(): Task[Vector[StocktakingItem]] = {
      val task = list(0, Int.MaxValue, document("date" -> -1))
      for {
        r <- task
      } yield {
        r.map(convert(_))
      }
    }

    def findOne(targetId: String): Task[Option[Stocktaking]] = {
      findOne(document("targetId" -> targetId), document("date" -> -1))
    }

    def find(targetId: String): Task[Vector[StocktakingItem]] = {
      val task = list(0, Int.MaxValue, document("targetId" -> targetId), document("date" -> -1))
      for {
        r <- task
      } yield {
        r.map(convert(_))
      }
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
          Project(document("date" -> 1, "amount" -> 1, "income" -> 1, "targetId" -> 1, "createTime" -> 1)),
          Group(BSONString("$targetId"))(
            "date" -> FirstField("date"),
            "amount" -> FirstField("amount"),
            "income" -> FirstField("income"),
            "createTime" -> FirstField("createTime")))
        coll.aggregatorContext[StocktakingStats](pipeline)
            .prepared
            .cursor
            .collect[Vector](Int.MaxValue, Cursor.FailOnError[Vector[StocktakingStats]]())
      })
    }

    def getById(id: String): Task[Option[StocktakingItem]] = {
      for {
        r <- super.get(id)
      } yield {
        r.map(convert(_))
      }
    }

    def delById(id: String) = {
      super.delete(id)
    }

    val convert: Stocktaking => StocktakingItem = stocktaking => {
      val dateFormat = DateTime.parse(stocktaking.date.toString, DateTimeFormat.forPattern("yyyyMM")).toString("yyyy-MM")
      StocktakingItem(stocktaking.targetId, stocktaking._id.get.stringify, dateFormat, stocktaking.amount, stocktaking.income, stocktaking.totalIncome, stocktaking.rate, new DateTime(stocktaking.createTime).toString("yyyy-MM-dd"), stocktaking.comment)
    }
  }

  val live: ZLayer[Any, Nothing, ZStocktakingEnv] = ZLayer.succeed(new Service)

  def save(command: CreateStocktakingCommand): ZIO[ZStocktakingEnv, Throwable, Boolean] = ZIO.accessM(_.get.save(command))

  def update(id: String, command: CreateStocktakingCommand): ZIO[ZStocktakingEnv, Throwable, Int] = ZIO.accessM(_.get.update(id, command))

  def find(): ZIO[ZStocktakingEnv, Throwable, Vector[StocktakingItem]] = ZIO.accessM(_.get.find())

  def findOne(targetId: String): ZIO[ZStocktakingEnv, Throwable, Option[Stocktaking]] = ZIO.accessM(_.get.findOne(targetId))

  def find(targetId: String): ZIO[ZStocktakingEnv, Throwable, Vector[StocktakingItem]] = ZIO.accessM(_.get.find(targetId))

  def aggregate(mat: BSONDocument)(implicit m: BSONDocumentReader[StocktakingStats], tag: ClassTag[Stocktaking]): ZIO[ZStocktakingEnv, Throwable, Vector[StocktakingStats]] = ZIO.accessM(_.get.aggregate(mat))

  def getById(id: String): ZIO[ZStocktakingEnv, Throwable, Option[StocktakingItem]] = ZIO.accessM(_.get.getById(id))

  def delById(id: String): ZIO[ZStocktakingEnv, Throwable, Int] = ZIO.accessM(_.get.delById(id))
}

