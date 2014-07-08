package com.stylekick.common.data

import play.api.libs.json.{JsObject, Json}
import models.ProductUrl
import scala.concurrent.Future
import com.google.inject.Singleton

//case class ProductUrl(outfitId: String, url: String)

trait ProductUrlDAO extends DataAccessObject[ProductUrl] {
  def findForOutfitId(id: String) : Future[List[JsObject]]
}

@Singleton
class ProductUrlMongoDAO extends ProductUrlDAO with MongoDAO[ProductUrl] {
  val collectionName = "product_urls"

  implicit val serializableFormat = ProductUrl.serializableFormat

  def findForOutfitId(id: String) : Future[List[JsObject]]  =  {
    find(Json.obj("outfitId" -> id))
  }

}
