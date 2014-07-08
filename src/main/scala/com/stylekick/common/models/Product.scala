package models

import play.api.libs.json.{JsObject, Json}
import org.parse4j.ParseObject
import java.util.Date


case class Product(
                    parseId: String,
                    accurate : Boolean,
                    brand: String,
                    parseImageV2Id: String,
                    keywords: String,
                    name: String,
                    price: Double,
                    shopUrl: String,
                    imageUrl: Option[String],
                    trashed : Boolean,
                    createdAt : Date,
                    updatedAt : Date,
                    points: Option[Points] = None,
                    outfitId: Option[String] = None,
                    status: Option[String] = None
) extends PFModel


object Product extends ParseModel[Product] {
  implicit val flagReads = Json.reads[Product]
  implicit val flagWrites = Json.writes[Product]

  def toJson(product: Product) = Json.toJson[Product](product)

  def buildObjectFromParse(pfObject : ParseObject) : Product = {
    Product(
      parseId = pfObject.getObjectId(),
      parseImageV2Id = pfObject.getParseObject("image").getObjectId(),
      accurate = pfObject.getBoolean("accurate"),
      brand = pfObject.getString("brand"),
      keywords = pfObject.getString("keywords"),
      name = pfObject.getString("name"),
      price = pfObject.getNumber("price").doubleValue(),
      shopUrl = pfObject.getString("shop_url"),
      imageUrl = Option(pfObject.getString("url")),
      trashed = Option(pfObject.getBoolean("trashed")).getOrElse(false),
      createdAt = pfObject.getCreatedAt,
      updatedAt = pfObject.getUpdatedAt,
      status = Option(pfObject.getString("status"))
    )
  }

  def fromParsePOST(json: JsObject) = {
    Product(
      parseId = (json \ "objectId").as[String],
      accurate = (json \ "accurate").asOpt[Boolean].getOrElse(true),
      keywords = (json \ "String").asOpt[String].getOrElse(""),
      parseImageV2Id = (json \ "image" \ "objectId").as[String],
      trashed = (json \ "trashed").asOpt[Boolean].getOrElse(false),
      name = (json \ "name").as[String],
      brand = (json \ "brand").as[String],
      shopUrl = (json \ "shop_url").as[String],
      price = (json \ "price").as[Double],
      imageUrl = (json \"url").asOpt[String],
      createdAt = (json \ "createdAt").as[Date],
      updatedAt = (json \ "updatedAt").as[Date],
      status = (json \ "status").asOpt[String]
    )
  }

}
