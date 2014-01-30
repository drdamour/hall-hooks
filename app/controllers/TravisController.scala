package controllers

import play.api.mvc._
import hall.HallCommandHandlerSlice
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json
import models.Travis.BuildMessage
import models.HallMessage
import play.api.i18n.Messages

case class TravisSimulation(request:String)

trait TravisController {
  this:Controller with HallCommandHandlerSlice =>

  def sendBuildStatusToHall(roomToken:String) = Action { implicit request =>
    //Get the json from the form body
    val json = Json.parse(request.body.asFormUrlEncoded.get("payload").head)

    //turn it into the much beloved case classes
    val o = Json.fromJson[BuildMessage](json).get

    //figure out what we want our message to be
    val hallMessage = HallMessage(
      roomToken,
      o.payload.repository.name + " project build status",
      s"Build Number ${o.payload.number} fininshed with ${o.payload.status_message}",
      None
    )

    sendMessage(hallMessage)

    Redirect(routes.TravisController.index()).flashing("success-message" -> Messages("notification.messageSentSuccessfully"))
  }

  //ripped from https://gist.github.com/svenfuchs/1225015
  val examplePayload =
    """
      |{
      |  "payload": {
      |    "id": 1,
      |    "number": 1,
      |    "status": null,
      |    "started_at": null,
      |    "finished_at": null,
      |    "status_message": "Passed",
      |    "commit": "62aae5f70ceee39123ef",
      |    "branch": "master",
      |    "message": "the commit message",
      |    "compare_url": "https://github.com/svenfuchs/minimal/compare/master...develop",
      |    "committed_at": "2011-11-11T11: 11: 11Z",
      |    "committer_name": "Sven Fuchs",
      |    "committer_email": "svenfuchs@artweb-design.de",
      |    "author_name": "Sven Fuchs",
      |    "author_email": "svenfuchs@artweb-design.de",
      |    "repository": {
      |      "id": 1,
      |      "name": "minimal",
      |      "owner_name": "svenfuchs",
      |      "url": "http://github.com/svenfuchs/minimal"
      |     },
      |    "matrix": [
      |      {
      |        "id": 2,
      |        "repository_id": 1,
      |        "number": "1.1",
      |        "state": "created",
      |        "started_at": null,
      |        "finished_at": null,
      |        "config": {
      |          "notifications": {
      |            "webhooks": ["http://evome.fr/notifications", "http://example.com/"]
      |          }
      |        },
      |        "status": null,
      |        "log": "",
      |        "result": null,
      |        "parent_id": 1,
      |        "commit": "62aae5f70ceee39123ef",
      |        "branch": "master",
      |        "message": "the commit message",
      |        "committed_at": "2011-11-11T11: 11: 11Z",
      |        "committer_name": "Sven Fuchs",
      |        "committer_email": "svenfuchs@artweb-design.de",
      |        "author_name": "Sven Fuchs",
      |        "author_email": "svenfuchs@artweb-design.de",
      |        "compare_url": "https://github.com/svenfuchs/minimal/compare/master...develop"
      |      }
      |    ]
      |  }
      |}
    """.stripMargin

  val travisSimulationForm = Form(
    mapping(
      "payload" -> nonEmptyText
    )(TravisSimulation.apply)(TravisSimulation.unapply)
  )

  def index = Action { implicit request =>
    Ok(views.html.Travis.info(travisSimulationForm.fill(TravisSimulation(examplePayload))))
  }

}

object TravisController extends Controller with TravisController with HallCommandHandlerSlice
