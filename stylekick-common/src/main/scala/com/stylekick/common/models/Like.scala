package models

import java.util.Date
import org.parse4j.ParseObject
import play.api.libs.json.Json


case class Points(x : Double, y: Double)

object Points {
  implicit val pointsReads = Json.reads[Points]
  implicit val pointsWrites = Json.writes[Points]
}

case class Like(
                 parseId : String,
                 parseUserId : String,
                 parseImageV2Id : String,
                 points: Points,
                 outfitId : Option[String] = None,
                 userId : Option[String] = None,
                 trashed : Boolean,
                 createdAt : Date,
                 updatedAt : Date,
                 username : Option[String] = None,
                 user : Option[User] =  None
               ) extends PFModel

object Like extends ParseModel[Like] {
  implicit val likeReads = Json.reads[Like]
  implicit val likeWrites = Json.writes[Like]

  def toJson(like: Like) = Json.toJson[Like](like)

  def buildObjectFromParse(pfLike : ParseObject) : Like = {
    val p = pfLike.get("points").asInstanceOf[java.util.HashMap[String,Double]]
    val x = p.get("x")
    val y = p.get("y")
    val points = Points(x,y)

    Like(
      parseId = pfLike.getObjectId,
      parseUserId = pfLike.getString("userId"),
      points = points,
      parseImageV2Id = pfLike.getString("imageId"),
      trashed = Option(pfLike.getBoolean("trashed")).getOrElse(false),
      createdAt = pfLike.getCreatedAt,
      updatedAt = pfLike.getUpdatedAt
    )
  }
}
