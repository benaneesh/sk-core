package com.stylekick.common.data

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule

/**
 * Created by Anand on 2014-06-23.
 */
class DataAccessModule extends AbstractModule with ScalaModule {
  def configure(): Unit = {
    bind[FutureLikeDAO].to[FutureLikeMongoDAO].asEagerSingleton()
    bind[LikeDAO].to[LikeMongoDAO].asEagerSingleton()
    bind[OutfitDAO].to[OutfitMongoDAO].asEagerSingleton()
    bind[UserDAO].to[UserMongoDAO].asEagerSingleton()
    bind[ProductDAO].to[ProductMongoDAO].asEagerSingleton()
    bind[ProductUrlDAO].to[ProductUrlMongoDAO].asEagerSingleton()
    bind[FlagDAO].to[FlagMongoDAO].asEagerSingleton()
  }
}
