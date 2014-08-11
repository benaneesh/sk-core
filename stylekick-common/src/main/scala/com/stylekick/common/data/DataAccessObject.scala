package com.stylekick.common.data

import com.typesafe.scalalogging.slf4j.LazyLogging
import play.api.libs.json._
import scala.concurrent.Future
import reactivemongo.core.commands.{Count, LastError}
import reactivemongo.api.QueryOpts
import reactivemongo.api.QueryOpts
import reactivemongo.api.QueryOpts
import play.api.libs.json.JsObject
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.json.BSONFormats
import reactivemongo.bson.BSONDocument
import com.typesafe.scalalogging.BaseLogger

/**
 * Created by Anand on 2014-05-20.
 */
trait DataAccessObject[T] extends LazyLogging {


  def upsert(selector : JsObject, update : JsValue) : Future[LastError]
  def upsert(selector : JsObject, model : T) : Future[LastError]
  def upsert(id : String, update : JsValue) : Future[LastError]
  def upsert(id : String, model : T) : Future[LastError]

  //  def update(selector : JsObject, set : JsValue)

  def find(query: JsObject, sort: Option[(String, Int)], limit : Option[Int], options : QueryOpts): Future[List[JsObject]]
  def find(query: JsObject, limit : Int) : Future[List[JsObject]]
  def find(query: JsObject = Json.obj()) : Future[List[JsObject]]
  def find(page: Int, selector : JsObject, sort: (String, Int), size : Int) : Future[List[JsObject]]

  def findWithCount(page: Int, selector : JsObject, sort: (String, Int), size : Int) : (Future[List[JsObject]], Future[Int])
  def findWithCount(page: Int, selector : JsObject, sort: String, size : Int) : (Future[List[JsObject]], Future[Int])

  def findOne(query: JsObject, sort: Option[(String, Int)], limit : Option[Int], options : QueryOpts): Future[Option[JsValue]]
  def findOne(query: JsObject = Json.obj()) : Future[Option[JsValue]]
  def findOne(query: JsObject, sort: (String, Int), options : QueryOpts) : Future[Option[JsValue]]
  def findOne(query: JsObject, sort: (String, Int)) : Future[Option[JsValue]]

  def findById(id: String) : Future[Option[JsValue]]
  def findById(ids: List[String]) : Future[List[JsObject]]
}

trait MongoDAO[T] extends MongoHelper {
  implicit val serializableFormat : Format[T]

  val toMongoUpsert = (__ \ '$set).json.copyFrom( __.json.pick )
  val toObjectId = OWrites[String]{ s => Json.obj("_id" -> Json.obj("$oid" -> s)) }
  val fromObjectId = (__ \ '_id).json.copyFrom( (__ \ '_id \ '$oid).json.pick )

  val collectionName : String
  def collection: JSONCollection = db.collection[JSONCollection](collectionName)

  def upsert(selector : JsObject, update : JsValue) : Future[LastError] = {
//    logger.debug(s"upsert {collection=$collectionName} [selector=$selector] [update=$update]")

    collection.update(selector,
      update,
      upsert = true
    )
  }

  def upsert(id : String, update : JsValue) : Future[LastError] = {
    upsert(toObjectId.writes(id), update)
  }

  def upsert(selector : JsObject, model : T) : Future[LastError] = {
    val json = Json.toJson[T](model)
    val update = json.transform(toMongoUpsert)
    upsert(selector, update.get)
  }

  def upsert(id : String, model : T) : Future[LastError] = {
    val selector = toObjectId.writes(id)
    upsert(selector, model)
  }


  def update(selector : JsObject, set : JsValue) = {
//    Logger.debug(s"update {collection=$collectionName} [selector=$selector] [set=$set]")

    collection.update(selector, set)
  }



  def find(query: JsObject, sort: Option[(String, Int)], limit : Option[Int], options : QueryOpts): Future[List[JsObject]] = {
//    Logger.debug(s"find {collection=$collectionName} [query=$query] [sort=$sort] [limit=$limit] [options=$options]")

    val q = collection.find(query)
    val sortedQ = sort match {
      case Some((key, direction)) => q.sort(Json.obj(key -> direction)).options(options).cursor[JsObject]
      case None => q.options(options).cursor[JsObject]
    }

    limit match{
      case Some(n) => sortedQ.collect[List](n)
      case None => sortedQ.collect[List]()
    }
  }



  def find(query: JsObject, limit : Int) : Future[List[JsObject]] = {
    find(query, None, Option(limit), QueryOpts())
  }

  def find(query: JsObject = Json.obj()) : Future[List[JsObject]] = {
    find(query,None,None,QueryOpts())
  }

  def find(page: Int, selector : JsObject, sort: (String, Int), size : Int) : Future[List[JsObject]] = {
    collection.
      find(selector).
      options(QueryOpts(batchSizeN = size, skipN = size * (page - 1))).
      sort(Json.obj(sort._1 -> sort._2)).
      cursor[JsObject].
      collect[List](size)
  }



  def findWithCount(page: Int, selector : JsObject, sort: (String, Int), size : Int) : (Future[List[JsObject]], Future[Int]) = {
    val query = BSONFormats.toBSON(selector).get.asInstanceOf[BSONDocument]
    val countFuture = db.command(Count(collectionName, Option(query)))
    val listFuture = find(page, selector, sort, size)

    (listFuture, countFuture)
  }

  def findWithCount(page: Int, selector : JsObject, sort: String, size : Int) : (Future[List[JsObject]], Future[Int]) = {
    findWithCount(page, selector, (sort, 1), size)
  }




  def findOne(query: JsObject, sort: Option[(String, Int)], limit : Option[Int], options : QueryOpts): Future[Option[JsValue]] = {
//    Logger.debug(s"findOne {collection=$collectionName} [query=$query] [sort=$sort] [limit=$limit] [options=$options]")

    val q = collection.find(query).options(options)
    val sortedQ = sort match {
      case Some((key, direction)) => q.sort(Json.obj(key -> direction))
      case None => q
    }
    sortedQ.one[JsValue]
  }

  def findOne(query: JsObject = Json.obj()) : Future[Option[JsValue]] =  {
    findOne(query, None, None, QueryOpts())
  }

  def findOne(query: JsObject, sort: (String, Int), options : QueryOpts) : Future[Option[JsValue]] = {
    findOne(query, Option(sort), None, options)
  }

  def findOne(query: JsObject, sort: (String, Int)) : Future[Option[JsValue]] = {
    findOne(query, Option(sort), None, QueryOpts())
  }

  def findById(id: String) : Future[Option[JsValue]] = {
    findOne(toObjectId.writes(id))
  }

  def findById(ids: List[String]) : Future[List[JsObject]] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> Json.obj( "$in" -> ids)))
//    Logger.info(s"#findById [ids=$ids] [query=$query]")

    find(query)
  }
}
