package com.stylekick.common.akkaguice

import akka.actor.{ActorRef, ActorContext}
import javax.inject.Singleton

trait ActorBuilder {
  def apply(context: ActorContext, actorName: String, name: String): ActorRef
}

@Singleton
class ActorBuilderImpl extends ActorBuilder {
  def apply(context: ActorContext, actorName: String, name: String): ActorRef = {
    context.actorOf(GuiceAkkaExtension(context.system).props(actorName), name)
  }
}
