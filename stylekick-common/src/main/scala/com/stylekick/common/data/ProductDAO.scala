package com.stylekick.common.data

import models.{Outfit, Product}
import play.api.libs.json.{JsObject, Json}
import scala.concurrent.Future
import reactivemongo.core.commands.LastError
import com.google.inject.Singleton

trait ProductDAO extends DataAccessObject[Product] with ParseLookup {
  def findForOutfitId(id: String) : Future[List[JsObject]]
}

@Singleton
class ProductMongoDAO extends ProductDAO with MongoDAO[Product] with ParseSelectors[Product]{
  val collectionName = "products"

  implicit val serializableFormat = Json.format[Product]

  def findForOutfitId(id: String) : Future[List[JsObject]]  =  {
    find(Json.obj("outfitId" -> id))
  }

  override def upsert(parseId: String, product: Product) : Future[LastError] = {
    val selector = toParseId.writes(parseId)
    upsert(selector, product)
  }
}
