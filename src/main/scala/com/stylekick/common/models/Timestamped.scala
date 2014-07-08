package models

import org.joda.time.DateTime


trait Timestamped {
  var created: Option[DateTime]
  var updated: Option[DateTime]
}
