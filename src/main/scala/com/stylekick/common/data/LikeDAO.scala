package com.stylekick.common.data

import play.api.libs.json.{JsObject, Json}
import models.Like
import scala.concurrent.Future
import com.google.inject.Singleton


trait LikeDAO extends DataAccessObject[Like] with ParseLookup {

  def findForOutfitId(id: String) : Future[List[JsObject]]
  def findByUserId(id: String) : Future[List[JsObject]]


}

@Singleton
class LikeMongoDAO extends LikeDAO with MongoDAO[Like] with ParseSelectors[Like]{
  val collectionName = "likes"



  implicit val serializableFormat = Json.format[Like]

  def findForOutfitId(id: String) : Future[List[JsObject]]  =  {
    find(Json.obj("outfitId" -> id))
  }

  def findByUserId(id: String) : Future[List[JsObject]] = {
    find(Json.obj("userId" -> id))
  }
}
