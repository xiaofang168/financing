package com.jeff.financing.internal

import spray.json.DefaultJsonProtocol

import scala.reflect.ClassTag

trait LowerCaseWithUnderscores extends DefaultJsonProtocol {
  override protected def extractFieldNames(classTag: ClassTag[_]) = {
    import java.util.Locale

    def format(name: String) = PASS2.replaceAllIn(PASS1.replaceAllIn(name, REPLACEMENT), REPLACEMENT).toLowerCase(Locale.US)

    super.extractFieldNames(classTag).map {
      format(_)
    }
  }

  private val PASS1 = """([A-Z]+)([A-Z][a-z])""".r
  private val PASS2 = """([a-z\d])([A-Z])""".r
  private val REPLACEMENT = "$1_$2"
}
