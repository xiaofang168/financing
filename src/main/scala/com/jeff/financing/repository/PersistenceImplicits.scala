package com.jeff.financing.repository

import com.jeff.financing.entity.{Account, Flow, Stocktaking}
import reactivemongo.api.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

object PersistenceImplicits {
  /**
   * account
   */
  implicit val accountReader: BSONDocumentReader[Account] = Macros.reader[Account]
  implicit val accountWriter: BSONDocumentWriter[Account] = Macros.writer[Account]

  /**
   * flow
   */
  implicit val flowReader: BSONDocumentReader[Flow] = Macros.reader[Flow]
  implicit val flowWriter: BSONDocumentWriter[Flow] = Macros.writer[Flow]

  /**
   * stocktaking
   */
  implicit val stocktakingReader: BSONDocumentReader[Stocktaking] = Macros.reader[Stocktaking]
  implicit val stocktakingWriter: BSONDocumentWriter[Stocktaking] = Macros.writer[Stocktaking]
}
