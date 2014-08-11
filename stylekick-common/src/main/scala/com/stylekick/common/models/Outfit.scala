package models

import reactivemongo.bson.BSONObjectID
import play.modules.reactivemongo.json.BSONFormats
import play.modules.reactivemongo.json.BSONFormats._

import com.stylekick.common.data.TimestampedModel
import org.joda.time.DateTime
import play.api.libs.json.{JsObject, Json, Format}
import java.util.Date
import org.parse4j.ParseObject

//case class Outfit(_id: BSONObjectID, PFId: String, likes: Int, imageUrl: String, PFUserId: String, status: String, sort: Option[Int] = None, trashed : Option[Boolean] = None) {
//  var created = None
//  var updated = None
//}


case class Resolution(width: Int, height: Int)

object Resolution {
  implicit val serializableFormat = Json.format[Resolution]
}

case class Outfit(
                 _id: Option[BSONObjectID] = None,
                 parseId: String,
                 createdAt : Date,
                 updatedAt : Date,
                 likes : Option[Int] = None,
                 imageUrl : Option[String] = None,
                 sort : Option[Int] = None,
                 parseUserId: String,
                 userId: Option[String] = None,
                 username: Option[String] = None,
                 status: String,
                 reason: Option[String] = None,
                 trashed: Boolean,
                 origin: Option[String] = None,
                 resolution: Option[Resolution] = None,
                 pushSent: Option[Boolean] = None
                ) extends PFModel




object Outfit extends ParseModel[Outfit] {
  import play.api.libs.json.Json

  // Generates Writes and Reads for Feed and User thanks to Json Macros
  implicit val serializableFormat = Json.format[Outfit]

  override def buildObjectFromParse(image: ParseObject): Outfit = {
    def getSort(image : ParseObject) : Option[Int] =  {
      image.getNumber("sort") match {
        case null => None
        case n : Number => Some(n.intValue())
      }
    }

    Outfit(
      parseId = image.getObjectId(),
      likes = Option(image.getInt("likes")),
      imageUrl = Option(image.getString("url")),
      parseUserId = image.getParseObject("user").getObjectId(),
      status = image.getString("status"),
      sort = getSort(image),
      trashed = Option(image.getBoolean("trashed")).getOrElse(false),
      createdAt = image.getCreatedAt,
      updatedAt = image.getUpdatedAt
    )
  }

  def fromParsePOST(json: JsObject) = {
    Outfit(
      parseId = (json \ "objectId").as[String],
      likes = (json \ "likes").asOpt[Int],
      origin = (json \ "Origin").asOpt[String],
      parseUserId = (json \ "user" \ "objectId").as[String],
      trashed = (json \ "trashed").asOpt[Boolean].getOrElse(false),
      pushSent = (json \ "pushSent").asOpt[Boolean],
      status = (json \ "status").as[String],
      sort = (json \ "sort").asOpt[Int],
      imageUrl = (json \"url").asOpt[String],
      createdAt = (json \ "createdAt").as[Date],
      updatedAt = (json \ "updatedAt").as[Date]
    )
  }
}

trait MongoSerializable[T] {
  implicit val serializableFormat : Format[T]
}
