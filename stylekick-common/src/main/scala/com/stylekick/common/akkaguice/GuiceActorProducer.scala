package com.stylekick.common.akkaguice

import com.google.inject.name.{Named, Names}
import com.google.inject.{Key, Injector}
import akka.actor.{Actor, IndirectActorProducer}

class GuiceActorProducer(val injector: Injector, val actorName: String) extends IndirectActorProducer {

  override def actorClass = classOf[Actor]

  override def produce() = {
    val name: Named = Names.named(actorName)
    val key: Key[Actor] = Key.get(classOf[Actor], name)
    val binding = injector.getBinding(key)
    binding.getProvider.get()
  }

}


