package com.jeff.financing.service

import com.jeff.financing._

object SortedTest {

  case class Book(a: Int, b: Option[Long], c: String, z: String)

  def main(args: Array[String]): Unit = {
    val r = sort(
      Seq[Book](
        Book(1, Some(5L), "foo1", "bar1"),
        Book(10, Some(50L), "foo10", "bar15"),
        Book(2, Some(3L), "foo3", "bar3"),
        Book(100, Some(52L), "foo4", "bar6"),
        Book(100, Some(51L), "foo4", "bar6"),
        Book(100, Some(51L), "foo3", "bar6"),
        Book(11, Some(15L), "foo5", "bar7"),
        Book(22, Some(45L), "foo6", "bar8")
      ),
      Seq(
        SortingField("a", Ordering[Int].reverse),
        SortingField[Option[Long]]("b", Ordering[Option[Long]].reverse),
        SortingField("c", Ordering[String].reverse)
      )
    )
    r.foreach(println)
  }

}
