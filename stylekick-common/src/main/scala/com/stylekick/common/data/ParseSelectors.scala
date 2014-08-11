package com.stylekick.common.data

import play.api.libs.json.{JsValue, Json, OWrites}
import models.PFModel
import scala.concurrent.Future

/**
 * Created by Anand on 2014-05-12.
 */
trait ParseSelectors[T] extends ParseHelpers with ParseLookup { this : MongoDAO[T] =>

  def findByParseId(parseId: String) : Future[Option[JsValue]]  = {
    findOne(toParseId.writes(parseId))
  }

}

trait ParseLookup {
  def findByParseId(parseId: String) : Future[Option[JsValue]]
}

trait ParseHelpers {
  val toParseId = OWrites[String]{ s => Json.obj("parseId" -> s) }

}
