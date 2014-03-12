package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages


import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future
import models._
import hall._


trait TestFormController extends Controller {
  this: HallCommandHandlerSlice =>

  val chatForm = Form(
    mapping(
      "roomToken" -> nonEmptyText(32, 32),
      "title" -> nonEmptyText,
      "message" -> nonEmptyText,
      "picture" -> optional(text)
    )(HallMessage.apply)(HallMessage.unapply)
  )


  def index = Action { implicit request =>
    Ok(views.html.testform(chatForm))
  }

  def sendTestMessage = Action.async { implicit request =>

    chatForm.bindFromRequest().fold(
      badForm => {
        Future(BadRequest(views.html.testform(badForm)))
      },
      validMessage => {
        hallCommandHandler.sendMessage(validMessage).map { response =>
          //TODO: any response other than success should be thrown down to recover

          Redirect(routes.TestFormController.index()).flashing("success-message" -> Messages("notification.messageSentSuccessfully"))
        }
        .recover {
          case e: Exception =>
            //If it didn't work, but the message was valid, re-populate the form and try again
            val f = Flash(Map("failure-message" -> "go to hell"))
            //I don't get how to override the implicit flash here, so i have to explicitly pass it in...and i'm starting to see the flaw
            //of having the view know about flash...need to find a better pattern..if there is one
            BadRequest(views.html.testform(chatForm.fill(validMessage))(f,lang))
        }
      }

    )
  }

}

object TestFormController extends TestFormController with HallCommandHandlerSlice {
  val hallCommandHandler = new HallCommandHandler
}