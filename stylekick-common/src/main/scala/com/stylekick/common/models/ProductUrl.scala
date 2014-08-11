package models

import play.api.libs.json.Json

case class ProductUrl(outfitId: String, url: String, accurate: Option[Boolean])

object ProductUrl {
  implicit val serializableFormat = Json.format[ProductUrl]
  implicit val flagReads = Json.reads[ProductUrl]
  implicit val flagWrites = Json.writes[ProductUrl]
}






