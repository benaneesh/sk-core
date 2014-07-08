package com.stylekick.common.data

import javax.inject.Singleton
import play.api.libs.json.{JsValue, Json, JsObject}
import org.joda.time.DateTime
import scala.concurrent.Future
import play.modules.reactivemongo.json.BSONFormats
import reactivemongo.bson.BSONDocument
import reactivemongo.core.commands.{LastError, Count}


case class FutureLike(parseImageId : String, outfitId : String, parseUserId: String, userId : String, time : DateTime, id : Option[String] = None)

trait FutureLikeDAO {
  def cancelAllForParseId(parseImageV2Id: String)

  def scheduleFutureLike(like : FutureLike)

  def findLikesToBeCreated : Future[Seq[FutureLike]]

//  def cancelLike(id : String) : Future[LastError]

  def setStatus(id : String, status : String) : Future[LastError]

  def getCountForParseId(id : String) : Future[Int]
}

@Singleton
class FutureLikeMongoDAO extends FutureLikeDAO with MongoDAO[FutureLike] with ParseHelpers {
  val collectionName : String = "future_likes"
  implicit val serializableFormat = Json.format[FutureLike]

  def cancelAllForParseId(parseImageV2Id: String) = {
    val selector = Json.obj("parseImageV2Id" -> parseImageV2Id)
    val set = Json.obj("$set" -> Json.obj("status" -> "cancelled"))
    collection.update(selector, set, multi = true)
  }

  def scheduleFutureLike(like : FutureLike) = {
    collection.insert(Json.toJson[FutureLike](like))
  }

  def findLikesToBeCreated : Future[Seq[FutureLike]] = {
    val now = DateTime.now()
    val selector = Json.obj("status" -> "queued", "time" -> Json.obj("$gt" -> now))
    find(selector).map{ list => list.map(_.as[FutureLike]) }
  }

//  def cancelLike(id : String) : Future[LastError] = {
//
//  }

  def setStatus(id : String, status : String) : Future[LastError] = {
    val update = Json.obj("$set" -> Json.obj("status" -> status))
    val set: JsValue = update
    val selector: JsObject = toObjectId.writes(id)

    val futureLike = collection.find(selector).one[JsObject]
    val lastError = collection.update(selector, set)

    lastError

  }

  def getCountForParseId(id : String) : Future[Int] = {
    val selector = toParseId.writes(id)
    val query = BSONFormats.toBSON(selector).get.asInstanceOf[BSONDocument]

    db.command(Count(collectionName, Option(query)))
  }
}
