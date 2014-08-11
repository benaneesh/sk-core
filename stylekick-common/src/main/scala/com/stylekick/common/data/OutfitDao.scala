package com.stylekick.common.data

import play.modules.reactivemongo.ReactiveMongoPlugin
import play.api.libs.json._
import play.modules.reactivemongo.json.BSONFormats
import play.modules.reactivemongo.json.BSONFormats._
import reactivemongo.bson.{BSONString, BSONDocument}
import reactivemongo.core.commands.{LastError, Count}
import reactivemongo.api.QueryOpts
import play.api.libs.json.JsObject
import play.api.libs.json.JsNumber
import models.{Resolution, Outfit}
import scala.concurrent.Future
//import play.api.Logger
import com.google.inject.Singleton


trait OutfitDAO extends DataAccessObject[Outfit] with ParseLookup {
  def listOutfits(page: Int, selector : JsObject, sort: String, size : Int) : (Future[List[JsObject]], Future[Int])
  def listOutfits(page: Int, selector : JsObject, sort: (String, Int), size : Int) : (Future[List[JsObject]], Future[Int])

  def findForUserId(id: String, status: String) : Future[List[JsObject]]

  def findPosition(outfit: JsObject) : Future[Int]

  //mutators
  def setImageUrl(id: String, imageUrl: String) : Future[LastError]
  def setStatus(id: String, status: String) : Future[LastError]
  def setPosition(id: String, position : Int) : Future[LastError]
  def setResolution(id: String, resolution : Resolution) : Future[LastError]



}

@Singleton
class OutfitMongoDAO extends OutfitDAO with MongoDAO[Outfit] with ParseSelectors[Outfit] {
  val collectionName = "outfits"

  implicit val serializableFormat = Outfit.serializableFormat

  def listOutfits(page: Int, selector : JsObject, sort: (String, Int), size : Int) : (Future[List[JsObject]], Future[Int]) = {
    val query = BSONFormats.toBSON(selector).get.asInstanceOf[BSONDocument]
    val countFuture = db.command(Count(collectionName, Option(query)))

    find(selector, limit = Some(size), sort = Some(sort._1, sort._2), options = QueryOpts(batchSizeN = size, skipN = size * (page - 1)))

    val listFuture = collection.
      find(selector).
      options(QueryOpts(batchSizeN = size, skipN = size * (page - 1))).
      sort(Json.obj(sort._1 -> sort._2)).
      cursor[JsObject].
      collect[List](size)

    (listFuture, countFuture)
  }

  def listOutfits(page: Int, selector : JsObject, sort: String, size : Int) : (Future[List[JsObject]], Future[Int]) = {
    listOutfits(page, selector, (sort, 1), size)
  }

  override def upsert(parseId: String, outfit: Outfit) : Future[LastError] = {
    val selector = toParseId.writes(parseId)
    upsert(selector, outfit)
  }

//  def getPositionInApp(id: String) : Future[Int] =  {
//    findById(id).map {
//      case Some(outfit) =>
//        val sort = (outfit \ "sort").as[Int]
//        val selector = Json.obj("sort" -> Json.obj("$gt" -> sort))
//        val query = BSONFormats.toBSON(selector).get.asInstanceOf[BSONDocument]
//        db.command(Count("outfits", Option(query)))
//      case None => -1
//    }
//  }

  def setImageUrl(id: String, imageUrl: String) : Future[LastError] = {
//    Logger.info(s"OutfitDAO#setImageUrl [id=$id] [imageUrl=$imageUrl]")

    val update = Json.obj("$set" -> Json.obj("imageUrl" -> imageUrl))
    val set: JsValue = update
    val selector: JsObject = toObjectId.writes(id)


    val outfit = collection.find(selector).one[JsObject]
    val lastError = collection.update(selector, set)

    lastError
  }

  def setStatus(id: String, status: String) : Future[LastError] = {
    val update = Json.obj("$set" -> Json.obj("status" -> status))
    val set: JsValue = update
    val selector: JsObject = toObjectId.writes(id)

    val outfit = collection.find(selector).one[JsObject]
    val lastError = collection.update(selector, set)

    lastError
  }

  def setPosition(id: String, position : Int) : Future[LastError] = {
    val update = Json.obj("$set" -> Json.obj("sort" -> position))
    val set: JsValue = update
    val selector: JsObject = toObjectId.writes(id)


    val outfit = collection.find(selector).one[JsObject]
    val lastError = collection.update(selector, set)

    lastError
  }

  def findPosition(outfit: JsObject) : Future[Int] = {
    val sort = (outfit \ "sort").as[Int]
    db.command(Count(collectionName, Some(BSONDocument("status" -> BSONString("approved"), "sort" -> BSONDocument("$gte" -> sort))) ))
  }

  def setResolution(id: String, resolution : Resolution) : Future[LastError] = {
//    Logger.info(s"OutfitDAO#setResolution [id=$id] [resolution=$resolution]")
    val selector: JsObject = toObjectId.writes(id)
    val set = Json.obj("$set" -> Json.obj("resolution" -> resolution))


    update(selector, set)
  }

  def findForUserId(id: String, status: String) = {
    find(Json.obj("userId" -> id, "status" -> status))
  }




}

