package com.stylekick.common.data

import models.User
import play.api.libs.json.Json
import scala.concurrent.Future
import reactivemongo.core.commands.LastError
import com.google.inject.Singleton

//object UserDAO extends BaseDAO[User] with MongoDAO[User]  with ParseSelectors[User]{
//  val collectionName = "users"
//
//  override implicit val serializableFormat = Json.format[User]
//
//    def upsert(parseId: String, user: User) : Future[LastError] = {
//      val selector = toParseId.writes(parseId)
//      upsert(selector, user)
//    }
//
//
//
//}


trait UserDAO extends DataAccessObject[User] with ParseLookup

@Singleton
class UserMongoDAO extends UserDAO with MongoDAO[User] with ParseSelectors[User] {
  val collectionName = "users"

  override implicit val serializableFormat = Json.format[User]

//  def upsert(parseId: String, user: User) : Future[LastError] = {
//    val selector = toParseId.writes(parseId)
//    upsert(selector, user)
//  }
}
