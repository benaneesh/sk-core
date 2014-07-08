package models

import java.util.Date

trait PFModel {
  def parseId : String
  def createdAt : Date
  def updatedAt : Date
  def trashed : Boolean
}
