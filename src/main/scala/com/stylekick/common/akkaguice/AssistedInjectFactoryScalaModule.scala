package com.stylekick.common.akkaguice

import com.google.inject.{Module, Binder}
import com.google.inject._
import net.codingwell.scalaguice.InternalModule
import net.codingwell.scalaguice.typeLiteral
import com.google.inject.assistedinject.FactoryModuleBuilder

/**
 * Created by Anand on 2014-06-17.
 */
trait AssistedInjectFactoryScalaModule[B <: Binder] extends Module {
  self: InternalModule[B] =>

  protected[this] def bindFactory[C: Manifest, F: Manifest]() {
    bindFactory[C, C, F]()
  }

  protected[this] def bindFactory[I: Manifest, C <: I : Manifest, F: Manifest]() {
    val builder = new FactoryModuleBuilder()

    binderAccess.install(
      builder.implement(typeLiteral[I], typeLiteral[C])
      .build(typeLiteral[F]))
  }
}
