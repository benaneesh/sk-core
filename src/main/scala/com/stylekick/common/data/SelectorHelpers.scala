package com.stylekick.common.data

import play.api.libs.json.Json

trait SelectorHelpers {
  val notTrashed = Json.obj("trashed" -> Json.obj("$ne" -> true))
  val trashedSelector = Json.obj("trashed" -> true)
}


