package com.jeff

import java.lang.reflect.Method

import scala.reflect.ClassTag

package object financing {

  case class SortingField[T](field: String, ord: Ordering[T])

  def sort[T](unsorted: Seq[T], fields: Seq[SortingField[_]])(implicit tag: ClassTag[T]): Seq[T] = {
    @inline def invokeGetter[A](field: Method, obj: T): A = field.invoke(obj).asInstanceOf[A]

    @inline def orderingByField[A](field: Method)(implicit ord: Ordering[A]): Ordering[T] = {
      Ordering.by[T, A](invokeGetter[A](field, _))
    }

    val clazz = tag.runtimeClass
    if (fields.nonEmpty) {
      val field = clazz.getMethod(fields.head.field)

      implicit val composedOrdering: Ordering[T] = fields.tail.foldLeft {
        orderingByField(field)(fields.head.ord)
      } { case (ordering, currentField) =>
        val field = clazz.getMethod(currentField.field)
        val subOrdering: Ordering[T] = orderingByField(field)(currentField.ord)

        new Ordering[T] {
          def compare(x: T, y: T): Int = {
            val upperLevelOrderingResult = ordering.compare(x, y)

            if (upperLevelOrderingResult == 0) {
              subOrdering.compare(x, y)
            } else {
              upperLevelOrderingResult
            }
          }
        }
      }

      unsorted.sorted(composedOrdering)
    } else {
      unsorted
    }
  }

}
