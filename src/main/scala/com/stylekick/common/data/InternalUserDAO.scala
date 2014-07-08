package com.stylekick.common.data

import play.api.libs.json.JsObject

//import services.internalusers.futurelikes.scheduler.UserIds


trait InternalUserDAO {
  def getAll : Seq[JsObject]
}
