package com.example

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class StylekickServiceActor extends Actor with StylekickService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}

abstract class Resource {
  def name: String
  def id: Int
}

//case class User(name: String, id: Int) extends Resource
//case class Outfit(name: String, id: Int) extends Resource
//case class Like(name: String, id: Int) extends Resource
//case class Following(name: String, id: Int) extends Resource
//case class Flag(name: String, id: Int) extends Resource
//case class SeenCollection(name: String, id: Int) extends Resource
//case class Installation(name: String, id: Int) extends Resource
//case class Product(name: String, id: Int) extends Resource
//case class ProductUrl(name: String, id: Int) extends Resource
//case class Comment(name: String, id: Int) extends Resource

// this trait defines our service behavior independently from the service actor
trait StylekickService extends HttpService {

  def createResource(resourceName: String): Route =
    post {
      respondWithMediaType(`application/json`) & complete {
        """{ "response": "Created a """ + resourceName + """." }"""
      }
    }

  def getResource(resourceName: String, id: Int): Route =
    get {
      respondWithMediaType(`application/json`) & complete {
        """{ "response": "Got """ + resourceName + " " + id + """." }"""
      }
    }

  def listResource(resourceName: String): Route =
    get {
      respondWithMediaType(`application/json`) & complete {
        """{ "response": "Listed all """ + resourceName + """." }"""
      }
    }

  def updateResource(resourceName: String, id: Int): Route =
    get {
      respondWithMediaType(`application/json`) & complete {
        """{ "response": "Updated """ + resourceName + " " + id + """." }"""
      }
    }

  def deleteResource(resourceName: String, id: Int): Route =
    get {
      respondWithMediaType(`application/json`) & complete {
        """{ "response": "Deleted """ + resourceName + " " + id + """." }"""
      }
    }

  def crudResource(resourceName: String): Route =
    pathEndOrSingleSlash {
      createResource(resourceName) ~
      listResource(resourceName)
    } ~
    path(IntNumber) { id =>
      getResource(resourceName, id) ~
      updateResource(resourceName, id) ~
      deleteResource(resourceName, id)
    }

  val myRoute =
    pathPrefix("user") {
      crudResource("User")
    } ~
    pathPrefix("outfit") {
      crudResource("Outfit") ~
      pathPrefix(IntNumber) { outfitId =>
        pathPrefix("product") {
          crudResource("Product")
        } ~
        pathPrefix("productUrl") {
          crudResource("ProductUrl")
        } ~
        pathPrefix("comment") {
          crudResource("Comment")
        }
      }
    } ~
    pathPrefix("like") {
      crudResource("Like")
    } ~
    pathPrefix("following") {
      crudResource("Following")
    } ~
    pathPrefix("flag") {
      crudResource("Flag")
    } ~
    pathPrefix("seenCollection") {
      crudResource("SeenCollection")
    } ~
    pathPrefix("installation") {
      crudResource("Installation")
    }
}