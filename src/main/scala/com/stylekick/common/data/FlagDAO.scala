package com.stylekick.common.data

import play.api.libs.json.Json
import models.Flag
import com.google.inject.Singleton


trait FlagDAO extends DataAccessObject[Flag] with ParseLookup


@Singleton
class FlagMongoDAO extends FlagDAO with MongoDAO[Flag] with ParseSelectors[Flag]{
  val collectionName = "flags"

  implicit val serializableFormat = Json.format[Flag]
}
