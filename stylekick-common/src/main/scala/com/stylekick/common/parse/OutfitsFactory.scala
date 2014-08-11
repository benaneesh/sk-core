package com.stylekick.common.parse

import models.Outfit
import org.parse4j.ParseObject

import scala.collection.mutable.ListBuffer

object OutfitsFactory {

  def buildFromParseResponse(results: java.util.List[Nothing]) : List[Outfit] = {
    var outfits : ListBuffer[Outfit] = ListBuffer()

    def getSort(image : ParseObject) : Option[Int] =  {
      image.getNumber("sort") match {
        case null => None
        case n : Number => Some(n.intValue())
      }
    }


    val it = results.iterator
    while(it.hasNext() ) {
      val image = it.next().asInstanceOf[ParseObject]
      val outfit = Outfit(
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
      outfits = outfits :+ outfit
    }

    outfits.toList
  }

}
