package com.stylekick.common.data

import play.modules.reactivemongo.ReactiveMongoPlugin


import scala.concurrent.{Future, ExecutionContext}
import reactivemongo.core.commands.{Count, LastError}
import reactivemongo.core.errors.DatabaseException


import play.api.libs.json._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.QueryOpts
import models.MongoSerializable
import play.modules.reactivemongo.json.BSONFormats
import reactivemongo.bson.BSONDocument


trait MongoHelper {
  implicit def ec: ExecutionContext = ExecutionContext.Implicits.global

  import reactivemongo.api._
//  import scala.concurrent.ExecutionContext.Implicits.global

  // gets an instance of the driver
  // (creates an actor system)
  val driver = new MongoDriver
  val connection = driver.connection(List("localhost"))

  // Gets a reference to the database "plugin"
  val db = connection("jumpsuit")

//  lazy val db = ReactiveMongoPlugin.db

}

//
//trait BaseDAO[T] extends DataAccessObject[T] with MongoDAO with MongoHelper with MongoSerializable[T] {
//
//
//
//
//
//
//
//  val PAGE_SIZE = 25
//
//
//
////  val index = Index(List(("parseId", Ascending)), Option("parseId"), unique = true)
////  collection.indexesManager.ensure(index)
//
//
//
//
//
//  def upsert(selector : JsObject, model : T) : Future[LastError] = {
//    val json = Json.toJson[T](model)
//    val update = json.transform(toMongoUpsert)
//    upsert(selector, update.get)
//  }
//
//
////
////  def trash(id: String) : Future[LastError] = {
////    collection.update(toObjectId.writes(id),
////      {"trashed" -> true}
////   )
////  }
//}
//
////trait BaseDao extends MongoHelper {
////
////  val collectionName: String
////
////  def ensureIndexes: Future[List[Boolean]]
////
////  def Recover[S](operation: Future[LastError])(success: => S): Future[Either[ServiceException, S]] = {
////    operation.map {
////      lastError => lastError.inError match {
////        case true =>
////          Logger.error(s"DB operation did not perform successfully: [lastError=$lastError]")
////          Left(DBServiceException(lastError))
////        case false =>
////          Right(success)
////      }
////    } recover {
////      case exception =>
////        Logger.error(s"DB operation failed: [message=${exception.getMessage}]")
////
////        // TODO: better failure handling here
////        val handling: Option[Either[ServiceException, S]] = exception match {
////          case e: DatabaseException => {
////            e.code.map(code => {
////              Logger.error(s"DatabaseException: [code=${code}, isNotAPrimaryError=${e.isNotAPrimaryError}]")
////              code match {
////                case 10148 => {
////                  Left(OperationNotAllowedException("", nestedException = e))
////                }
////                case 11000 => {
////                  Left(DuplicateResourceException(nestedException = e))
////                }
////              }
////            })
////          }
////        }
////        handling.getOrElse(Left(UnexpectedServiceException(exception.getMessage, nestedException = exception)))
////    }
////  }
////
////}
