package hall

import play.api._
import play.api.libs.ws._
import play.api.libs.json._
import models._

trait HallCommandHandlerSlice {

  implicit val hallMessageRes = Json.format[HallMessage]

  //TODO: this isn't really a command handler because it doesn't use polymorph routing..but it's good enough for now

  def sendMessage(message:HallMessage) = {
    val hallBaseUrl = Play.current.configuration.getString("hall.hookEndpoint").get

    WS.url(hallBaseUrl + message.roomToken).withHeaders("Content-Type" -> "application/json").post(
      Json.toJson(message)
    )

  }

}
