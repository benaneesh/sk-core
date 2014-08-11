package models

import play.api.libs.json.Json

case class OutfitView(
                       parseUserId : String,
                       parseImageV2Id : String,
                       userId : Option[String] = None,
                       OutfitId : Option[String] = None
                       )

object OutfitView {
  implicit val viewReads = Json.reads[OutfitView]
  implicit val viewWrites = Json.writes[OutfitView]

  def toJson(view: OutfitView) = {
    Json.toJson[OutfitView](view)
  }
}