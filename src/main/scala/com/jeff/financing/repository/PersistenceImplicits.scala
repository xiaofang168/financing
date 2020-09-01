package com.jeff.financing.repository

import com.jeff.financing.entity.Account
import reactivemongo.api.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}

object PersistenceImplicits {
  /**
   * account
   */
  implicit val accountReader: BSONDocumentReader[Account] = Macros.reader[Account]
  implicit val accountWriter: BSONDocumentWriter[Account] = Macros.writer[Account]
}
