package com.jeff.financing.repository

import com.jeff.financing.entity.{Account, Flow, MonthlyReport, Stocktaking}
import com.jeff.financing.vo.{Capital, CapitalInterest, Income}
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

  /**
   * capital
   */
  implicit val capitalReader: BSONDocumentReader[Capital] = Macros.reader[Capital]
  implicit val capitalWriter: BSONDocumentWriter[Capital] = Macros.writer[Capital]

  /**
   * CapitalInterest
   */
  implicit val capitalInterestReader: BSONDocumentReader[CapitalInterest] = Macros.reader[CapitalInterest]
  implicit val capitalInterestWriter: BSONDocumentWriter[CapitalInterest] = Macros.writer[CapitalInterest]

  /**
   * Income
   */
  implicit val incomeReader: BSONDocumentReader[Income] = Macros.reader[Income]
  implicit val incomeWriter: BSONDocumentWriter[Income] = Macros.writer[Income]

  /**
   * monthlyReport
   */
  implicit val monthlyReportReader: BSONDocumentReader[MonthlyReport] = Macros.reader[MonthlyReport]
  implicit val monthlyReportWriter: BSONDocumentWriter[MonthlyReport] = Macros.writer[MonthlyReport]

}
