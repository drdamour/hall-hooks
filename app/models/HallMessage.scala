package models

case class HallMessage(roomToken:String, title:String, message:String, picture:Option[String])