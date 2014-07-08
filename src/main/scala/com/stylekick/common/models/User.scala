package models

import scala.collection.mutable.ListBuffer
import org.parse4j.ParseUser
import reactivemongo.bson.BSONObjectID
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import java.util.Date
import com.typesafe.scalalogging._


//authdata
case class User(
                 parseId: String,
                 username : String,
                 createdAt : Date,
                 updatedAt : Date,
                 trashed : Boolean,
                 blogUrl : Option[String] = None,
                 email: Option[String] = None,
                 fbId : Option[String] = None,
                 gender : Option[String] = None,
                 location: Option[String] = None,
                 name: Option[String] = None,
                 outfitViewCount: Option[Int] = None,
                 twitter: Option[String] = None,
                 twitterId: Option[Int] = None,
                 uploadCount: Option[Int] = None
                ) extends PFModel {

}




object User {
  implicit val userReads = Json.reads[User]
  implicit val userWrites = Json.writes[User]

  var children = Vector[AnyRef]()



  def hasMany[T](model : ParseModel[T]) : Unit = {
    children = children :+ model
//    logger.info(s"$children")
    val name = this.getClass.getSimpleName.dropRight(1)

    val modelName = model.getClass.getSimpleName.dropRight(1)
//    logger.info(name)
//    logger.info(modelName)
  }

  hasMany[Outfit](Outfit)
  hasMany[Like](Like)




  def buildFromParseResponse(results: java.util.List[Nothing]) : List[User] = {
    var users : ListBuffer[User] = ListBuffer()
    var views : ListBuffer[OutfitView] = ListBuffer()

    println("build from parse response")

    val it = results.iterator
    while(it.hasNext() ) {
      val pfUser = it.next().asInstanceOf[ParseUser]
      val user = User(
        parseId = pfUser.getObjectId,
        username = pfUser.getUsername,
        blogUrl = Option(pfUser.getString("blog_url")),
        email = Option(pfUser.getEmail),
        fbId = Option(pfUser.getString("fbId")),
        gender = Option(pfUser.getString("gender")),
        location = Option(pfUser.getString("location")),
        name = Option(pfUser.getString("name")),
        outfitViewCount = Option(Option(pfUser.getNumber("outfits_viewed")).map{_.intValue()}.getOrElse(0)),
        trashed = Option(pfUser.getBoolean("trashed")).getOrElse(false),
        createdAt = pfUser.getCreatedAt,
        updatedAt = pfUser.getUpdatedAt
      )

//      val seen = Option(pfUser.getList("seen"))
//
//
//
//      seen match {
//        case Some(list) =>
//          val length = list.size()
////          println(s"user has seen $length outfits")
//
//          val seenIterator = list.iterator()
//
//          while(seenIterator.hasNext()) {
//            val view = OutfitView(user.parseId, seenIterator.next().asInstanceOf[String])
//            views = views :+ view
//          }
//        case None =>
////          println(s"user has seen no outfits")
//          ;
//      }


      users = users :+ user
    }
    users.toList
  }

  def getTheJson(user: User) : JsValue = {
    Json.toJson[User](user)
  }


  def fromParsePOST(json: JsObject) = {
    User(
      parseId = (json \ "objectId").as[String],
      username = (json \ "username").as[String],
      email = (json \ "email").asOpt[String],
      trashed = (json \ "trashed").asOpt[Boolean].getOrElse(false),
      createdAt = (json \ "createdAt").as[Date],
      updatedAt = (json \ "updatedAt").as[Date],
      blogUrl = (json \ "blog_url").asOpt[String],
      fbId = (json \ "fbId").asOpt[String],
      gender = (json \ "gender").asOpt[String],
      name = (json \ "name").asOpt[String],
      twitter = (json \ "twitter").asOpt[String],
      twitterId = (json \ "twitter_id").asOpt[Int]
    )
  }



  val validateUser : Reads[JsObject] = (
      (__ \ 'PfId).json.pickBranch and
      (__ \ 'username).json.pickBranch and
      (__ \ 'email).json.pickBranch
    ).reduce

}
