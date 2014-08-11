package models

import scala.collection.mutable.ListBuffer
import org.parse4j.ParseObject
import play.api.libs.json.Json
import java.util.Date
//import play.api.Logger


case class Flag(
                parseId : String,
                parseImageV2Id: String,
                parseUserId: String,
                createdAt : Date,
                updatedAt : Date,
                trashed : Boolean,
                reason : Option[String] = None,
                outfitId: Option[String] = None,
                userId : Option[String] = None
) extends PFModel

object Flag extends ParseModel[Flag] {
  implicit val flagReads = Json.reads[Flag]
  implicit val flagWrites = Json.writes[Flag]

  def toJson(flag: Flag) = Json.toJson[Flag](flag)


  def buildObjectFromParse(pfObject : ParseObject) : Flag = {
    Flag(
      parseId = pfObject.getObjectId(),
      parseImageV2Id = pfObject.getParseObject("image").getObjectId(),
      parseUserId = pfObject.getParseObject("user").getObjectId(),
      reason = Option(pfObject.getString("reason")),
      createdAt = pfObject.getCreatedAt,
      updatedAt = pfObject.getUpdatedAt,
      trashed = Option(pfObject.getBoolean("trashed")).getOrElse(false)
    )
  }
}

abstract class ParseModel[T] {
  def buildObjectFromParse(pfObject : ParseObject) : T

  def buildFromParseResponse(results: java.util.List[Nothing]) : List[T] = {
    var listBuffer : ListBuffer[T] = ListBuffer()
    val safeResults = Option(results)

    safeResults match {
      case None =>
//        Logger.error("found null pointer")

      case Some(results) =>
        val it = results.iterator
        while(it.hasNext() ) {
          val pfLike = it.next().asInstanceOf[ParseObject]
          val model = buildObjectFromParse(pfLike)
          listBuffer = listBuffer :+ model
        }
    }
    listBuffer.toList
  }
}


