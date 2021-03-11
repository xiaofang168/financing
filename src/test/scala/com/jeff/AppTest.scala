package com.jeff


import java.io.{BufferedReader, FileReader}

import cats.implicits._
import org.joda.time.{DateTime, Days}
import org.junit._

import scala.util.{Try, Using}

@Test
class AppTest {

  @Test
  def testOK() = {
    val b = BigDecimal(0.888200000)
    println((b * 100).underlying.stripTrailingZeros())
  }

  @Test
  def dasy(): Unit = {
    val days = Days.daysBetween(new DateTime(1583424000000L), new DateTime(1599321600000l)).getDays
    println(days)
  }

  @Test
  def cats(): Unit = {
    val list = List(Some(1), Some(2), None, Some(3))
    val traversed = list.traverse(identity)
    println(traversed)
  }

  @Test
  def read(): Unit = {
    val lines: Try[Seq[String]] = Using.Manager { use =>
      val r1 = use(new BufferedReader(new FileReader("file1.txt")))
      val r2 = use(new BufferedReader(new FileReader("file2.txt")))
      val r3 = use(new BufferedReader(new FileReader("file3.txt")))
      val r4 = use(new BufferedReader(new FileReader("file4.txt")))

      // use your resources here
      def lines(reader: BufferedReader): Iterator[String] =
        Iterator.continually(reader.readLine()).takeWhile(_ != null)

      (lines(r1) ++ lines(r2) ++ lines(r3) ++ lines(r4)).toList
    }
    lines.map(e => e.foreach(println(_)))
  }

}


