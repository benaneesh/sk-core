package com.stylekick.common.data

import org.joda.time.DateTime
import reactivemongo.bson.BSONObjectID

trait IdentifiableModel {
  var _id: Option[BSONObjectID]

  def identify = _id.map(value => value.stringify).getOrElse("")
}

trait TimestampedModel extends IdentifiableModel {
  var created: Option[DateTime]
  var updated: Option[DateTime]
}
