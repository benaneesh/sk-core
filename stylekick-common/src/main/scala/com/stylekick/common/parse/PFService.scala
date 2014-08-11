package com.stylekick.common.parse

import java.util

import com.google.inject.Singleton
import com.typesafe.scalalogging.Logging
import com.typesafe.scalalogging.slf4j.Logger
import models._
import org.parse4j._
import org.parse4j.callback.{CountCallback, FindCallback, SaveCallback}
import org.slf4j.LoggerFactory

import scala.concurrent.{Future, promise}

case class ParseConfig(parseApplication : String, parseKey : String)

trait LikesApi {
  def getLikeCount(id : String) : Future[Int]

  def createLike(imageId : String, userId : String) : Like

  def hasUserLikedImage(userId : String, imageId : String) : Future[Boolean]

  def getLikes(lastObjectId : Option[String]) : Future[List[Like]]
}


trait AuthApi {



}


case class ProductFields(
                          parseImageV2Id: String,
                          status: String,
                          name : String,
                          brand : String,
                          keywords : String,
                          price : Double,
                          shopUrl : String,
                          url : String,
                          imageUrl : Option[String] = None,
                          outfitId : Option[String] = None
                          )


trait OutfitsApi {
  def initializeOutfit(userId : String) : String
  def updateOutfitUrl(parseImageV2Id : String, url : String) : Unit
  def updateOutfitPosition(id : String, position : Int) : Future[Boolean]
  def updateOutfitStatus(id : String, status : String) : Future[Boolean]
  def updateOutfitReason(id : String, reason : String) : Future[Boolean]

  def getOutfits(lastObjectId : Option[String]) : Future[List[Outfit]]
  def getWaitingOutfits(lastObjectId : Option[String]) : Future[List[Outfit]]

  def trashOutfit(parseImageV2Id : String) : Future[Boolean]
}

trait ProductsApi {
  def getProducts(lastObjectId : Option[String]) : Future[List[Product]]

  def findOrInitializeProduct(p : ProductFields) : String
  def updateProductUrl(parseProductId : String, url : String)


}

trait UsersApi {

  //change to user last objectId
//  def getUsers(page: Int = 1) : Future[(List[User], List[OutfitView])]

  def getUsers(lastObjectId : Option[String]) : Future[List[User]]

  def userCount : Future[Int]

}

trait FlagsApi {
  def getFlags(lastObjectId : Option[String]) : Future[List[Flag]]

}

trait PushApi {
  def sendPush(id: String, content: String)

}










trait ParseApi extends LikesApi with PushApi with UsersApi with FlagsApi with ProductsApi with OutfitsApi with ParseApiConfig



trait ParseApiConfig {
  val PAGE_SIZE = 1000
  val outfitClass = "Image_v2"
  val userClass: String = "users"
  val likeClass: String = "Like_v2"
  val flagClass: String = "Flag_v2"
  val productClass: String = "Product_v2"


//  def setup(parseApplication : String, parseKey : String) : Unit = {
//    Parse.initialize(parseApplication, parseKey)
//  }

}


@Singleton
class PFService(parseApplication : String, parseKey : String) extends ParseApi with Logging {
  val logger = Logger(LoggerFactory.getLogger("parse"))

  Parse.initialize(parseApplication, parseKey)


  def hasUserLikedImage(userId: String, imageId: String): Future[Boolean] = {
    val p = promise[Boolean]

    val query = ParseQuery.getQuery(likeClass)
    query.whereEqualTo("image", ParseObject.createWithoutData("Image_v2", imageId))
    query.whereEqualTo("user", ParseObject.createWithoutData("_User", userId))
    println(s"$query")

    query.findInBackground(new FindCallback {
      def done(results: util.List[Nothing], p2: ParseException): Unit = {
        println(s"$results")
        println(s"$p2")
        if(results == null || results.isEmpty())
          p.success(false)
        else
          p.success(true)
      }
    })

    p.future

  }

  def createLike(imageId: String, userId: String): Like = {
    val like = new ParseObject("Like_v2")

    like.put("user", ParseObject.createWithoutData("_User", userId))
    like.put("image", ParseObject.createWithoutData("Image_v2", imageId))
    like.save()

    Like.buildObjectFromParse(like)
  }

  def getLikeCount(id: String): Future[Int] = {
    val p = promise[Int]

    val query = ParseQuery.getQuery(likeClass)
    query.whereEqualTo("image", ParseObject.createWithoutData("Image_v2", id))
    query.countInBackground(new CountCallback(){
      override def done(count: Integer, parseException: ParseException): Unit = {
        p success count
      }
    })

    p.future

  }

  def findOrInitializeProduct(p : ProductFields) : String = {
    logger.info(s"findOrInitializeProduct [p=$p]")

    val query = ParseQuery.getQuery(productClass)
    query.whereEqualTo("image", ParseObject.createWithoutData(outfitClass, p.parseImageV2Id))
    query.whereEqualTo("shop_url", p.shopUrl)
    val results = query.find()

    logger.info(s"results = $results")

    var product : ParseObject = null

    if(results == null || results.size() == 0) {
      //initialize
      product = new ParseObject(productClass)
      product.put("image", ParseObject.createWithoutData(outfitClass, p.parseImageV2Id) )
    }
    else {
      //update
      product = results.get(0)
      logger.info(s"Found existing product: $product")
      product
    }

    product.put("status", "needs upload")
    product.put("name", p.name)
    product.put("brand", p.brand)
    product.put("keywords", p.brand)
    product.put("price", p.price)
    product.put("shop_url", p.shopUrl)
    product.put("url", p.url)
    product.save()

    product.getObjectId()
  }

  def updateProductUrl(parseProductId : String, url : String) = {
    logger.info(s"updateProductUrl [parseProductId=$parseProductId] [url=$url]")


    val query = ParseQuery.getQuery(productClass)
    val product : ParseObject = query.get(parseProductId)
    product.put("url", url)

    product.put("status", "active")
    product.save()

    logger.info(s"image=$product")
  }




  def initializeOutfit(userId : String) : String = {
    val outfit = new ParseObject(outfitClass)
    val query = ParseQuery.getQuery(userClass)
    val user : ParseUser = query.get(userId)

    outfit.put("user", ParseObject.createWithoutData("_User", userId))
    outfit.put("status", "needs upload")
    outfit.put("Origin", "manual")
    outfit.save()

    outfit.getObjectId()
  }

  def updateOutfitUrl(parseImageV2Id : String, url : String) = {
    val query = ParseQuery.getQuery(outfitClass)
    val image : ParseObject = query.get(parseImageV2Id)
    image.put("url", url)

    image.put("status", "approved")
    image.save()

    logger.info(s"image=$image")
  }

  def trashOutfit(parseImageV2Id : String) = {
    val p = promise[Boolean]

    val query = ParseQuery.getQuery(outfitClass)
    val image : ParseObject = query.get(parseImageV2Id)
    image.put("trashed", true)
    image.save()

    image.saveInBackground(new SaveCallback() {
      override def done(e: ParseException) {
        if(e == null)
          p.success(true)
        else
          p.failure(e)
      }
    })

    p.future
  }

  def getOutfitsWithCondition(page: Int = 1, key: Option[String] = None, value: AnyRef = None) = {
    val p = promise[Seq[Outfit]]()

    val query = ParseQuery.getQuery(outfitClass)
    key match {
      case Some(k) => query.whereEqualTo(k, value)
      case None => None
    }
    query.limit(PAGE_SIZE)
    query.skip(PAGE_SIZE * (page-1))

    query.findInBackground(new FindCallback() {
      override def done(results: java.util.List[Nothing], e: ParseException): Unit = {
        val outfits = OutfitsFactory.buildFromParseResponse(results)
        p success outfits
      }
    })
    p.future
  }


  def getWaitingOutfits(lastObjectId : Option[String]) : Future[List[Outfit]] = get[Outfit](outfitClass, Outfit.buildFromParseResponse _, lastObjectId, Option(("status", "waiting")))
  def getOutfits(lastObjectId : Option[String]) : Future[List[Outfit]] = get[Outfit](outfitClass, Outfit.buildFromParseResponse _, lastObjectId)
  def getLikes(lastObjectId : Option[String]) : Future[List[Like]] = get[Like](likeClass, Like.buildFromParseResponse _, lastObjectId)
  def getFlags(lastObjectId : Option[String]) : Future[List[Flag]] = get[Flag](flagClass, Flag.buildFromParseResponse _, lastObjectId)
  def getProducts(lastObjectId : Option[String]) : Future[List[Product]] = get[Product](productClass, Product.buildFromParseResponse _, lastObjectId)
  def getUsers(lastObjectId : Option[String]) : Future[List[User]] = get[User](userClass, User.buildFromParseResponse _, lastObjectId)

  //add covariance restriction on T
  //create abstract class ParseModel
  /**
   *
   * @param className the className on parse to be downloaded
   * @param factoryFunction a function that converts the results returned by parse to a list of case classes
   * @param lastObjectId the last objectid returned by parse - the results will begin with the next object id
   * @tparam T the case class that the results should get converted to
   * @return a list of the converted model
   */
  def get[T<:PFModel](className: String, factoryFunction : (java.util.List[Nothing]) => List[T],lastObjectId : Option[String], condition: Option[(String, Any)] = None): Future[List[T]] = {
    logger.info(s"get [class=$className] [factory=$factoryFunction] [lastObjectId=$lastObjectId]")

    val p = promise[List[T]]()

    val query = ParseQuery.getQuery(className)
    query.addAscendingOrder("objectId")
    query.limit(PAGE_SIZE)

    lastObjectId match {
      case Some(id) =>
        query.whereGreaterThan("objectId", id)
      case None =>
    }

    condition match {
      case Some((key, value)) =>
        query.whereEqualTo(key, value)
      case None =>
    }

    println(s"About to query in background")
    query.findInBackground(new FindCallback() {
      override def done(results: java.util.List[Nothing], e: ParseException): Unit = {
        println(s"Got results from parse [results=] [exception=$e]")

        val listBuffer = factoryFunction(results)
        p success listBuffer.toList
      }
    })

    p.future
  }

  def getOutfits(page: Int = 1) : Future[Seq[Outfit]] = {
    getOutfitsWithCondition(page)
  }

  def updateOutfitStatus(id : String, status : String) = {
    val p = promise[Boolean]()

    val result : Option[ParseObject] = Option(ParseQuery.getQuery(outfitClass).get(id))
    result match {
      case Some(image) =>
        image.put("status", status)
        image.save()
        p.success(true)
      case None =>
        p.failure(new RuntimeException(s"Can't find matching ImageV2 for id=$id"))
    }
    p.future
  }

  def updateOutfitPosition(id : String, position : Int) = {
    val p = promise[Boolean]()

    logger.info(s"updating outfit position in parse [id=$id] [position=$position]")

    val result : Option[ParseObject] = Option(ParseQuery.getQuery(outfitClass).get(id))
    result match {
      case Some(image) =>
        image.put("sort", position)
        logger.info("About to save to parse")
        image.save()
        logger.info("Successfully updated sort on parse")
        p.success(true)
      case None =>
        logger.info("Could not find image on parse")
        p.failure(new RuntimeException(s"Can't find matching ImageV2 for id=$id"))
    }
    p.future
  }

  def updateOutfitReason(id : String, reason : String) = {
    val p = promise[Boolean]()

    val result : Option[ParseObject] = Option(ParseQuery.getQuery(outfitClass).get(id))
    result match {
      case Some(image) =>
        image.put("reason", reason)
        image.save()
        p.success(true)
      case None =>

        p.failure(new RuntimeException(s"Can't find matching ImageV2 for id=$id"))
    }
    p.future
  }

  def createOutfit(outfit: Outfit) = {
    val pfOutfit = ParseObject.create(outfitClass)
    pfOutfit.save()
  }



//  def getUsers(page: Int = 1) : Future[(List[User], List[OutfitView])] = {
//    logger.info(s"#getUsers [page=$page]")
//
//    val p = promise[(List[User], List[OutfitView])]()
//
//    val query = ParseQuery.getQuery(userClass)
//    query.limit(PAGE_SIZE)
//    query.skip(PAGE_SIZE * (page-1))
//
//    println("about to run query")
//    query.findInBackground(new FindCallback() {
//      override def done(results: util.List[Nothing], p2: ParseException): Unit = {
//        println(s"Got results [results=$results] [exception=$p2]")
//        val users = User.buildFromParseResponse(results)
//        p success users
//      }
//    })
//
//    p.future
//  }



  def userCount : Future[Int] = {
    getCount(userClass)
  }

  def outfitCount : Future[Int] = {
    getCount(outfitClass)
  }

  def likeCount : Future[Int] = {
    getCount(likeClass)
  }

  def productCount: Future[Int] = {
    getCount(productClass)
  }

  def sendPush(id: String, content: String) = {
    logger.debug(s"PFService#sendPush [id=$id] [content=$content]")

    val push = new ParsePush

//    push.setData(JSONObject(Map("alert" -> content)))
    push.setMessage(content)
    push.setChannel(s"user_$id")
    logger.info(push.toString)
    push.send
  }

  private def getCount(className: String) : Future[Int] = {
    logger.info(s"#getCount [className=$className]")
    val p = promise[Int]

    val query = ParseQuery.getQuery(className).countInBackground(new CountCallback(){
      override def done(count: Integer, parseException: ParseException): Unit = {
        p success count
      }
    })

    p.future
  }
}
