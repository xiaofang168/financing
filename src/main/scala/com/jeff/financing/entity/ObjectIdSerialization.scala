package com.jeff.financing.entity

import reactivemongo.api.bson.BSONObjectID
import spray.json.{DefaultJsonProtocol, _}

trait ObjectIdSerialization extends DefaultJsonProtocol {

  val objectIDRegEx = "^[0-9a-fA-F]{24}$".r

  def isObjectIDValid(input: String): Boolean = (objectIDRegEx findFirstIn input).nonEmpty

  implicit object ObjectIdJsonFormat extends RootJsonFormat[BSONObjectID] {
    def write(iod: BSONObjectID): JsValue = {
      new JsString(iod.stringify)
    }

    def read(jsValue: JsValue): BSONObjectID = {
      jsValue match {
        case JsString(oid) =>
          if (isObjectIDValid(oid)) {
            BSONObjectID.parse(oid).get
          } else {
            throw DeserializationException(s"[$oid] is not a valid ObjectID")
          }
        case _ =>
          throw DeserializationException("String expected")
      }
    }
  }

}