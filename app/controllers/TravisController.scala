package controllers

import play.api.mvc._
import hall.HallCommandHandlerSlice
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json
import models.Travis.BuildMessage
import models.HallMessage
import play.api.i18n.Messages
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

case class TravisSimulation(roomToken:String, payload:String)

trait TravisController {
  this:Controller with HallCommandHandlerSlice =>

  private def doStuff(roomToken:String, payloadJSON:String) = {
    //Get the json from the form body
    val json = Json.parse(payloadJSON)

    //TODO: check results of validation and error if it's bad
    val validation = json.validate[BuildMessage]

    //turn it into the much beloved case classes
    val payload = Json.fromJson[BuildMessage](json).get

    //figure out what we want our message to be
    val messageText = payload match {
      //TODO: is there a better way to match based on a single property of a case class?
      case p if p.status_message.toLowerCase == "pending" => s"""<a target="_blank" href="${payload.build_url}">Build ${payload.number}</a> for branch <a target="_blank" href="${payload.repository.url}/${payload.branch}" >${payload.branch}</a> <b>started</b> (<a target="_blank" href="${payload.compare_url}">${payload.commit.substring(0, 6)}</a> by ${payload.committer_name})"""
      case _ => s"""<a target="_blank" href="${payload.build_url}">Build ${payload.number}</a> for branch <a target="_blank" href="${payload.repository.url}/${payload.branch}" >${payload.branch}</a> completed with status <b>${payload.status_message.toUpperCase}</b> (<a target="_blank" href="${payload.compare_url}">${payload.commit.substring(0, 6)}</a> by ${payload.committer_name})"""
    }

    val hallMessage = HallMessage(
      roomToken,
      payload.repository.name + " project build status",
      messageText,
      None
    )

    sendMessage(hallMessage).map { response =>
      Redirect(routes.TravisController.index()).flashing("success-message" -> Messages("notification.messageSentSuccessfully"))
    }.recover {
      case e: Exception =>
        BadRequest(e.getLocalizedMessage)
    }

  }

  def sendBuildStatusToHall(roomToken:String) = Action.async { implicit request =>
  //TODO: add some verification against stuff being present or not

  //Get payload param
    val payloadJSON = request.body.asFormUrlEncoded.get("payload").head

    //Log the payload message
    Logger("application.controllers.TravisController").debug(payloadJSON)

    doStuff(roomToken, payloadJSON)

  }

  //originally ripped from https://gist.github.com/svenfuchs/1225015 but i found diff form what was really happening, so this
  val examplePayload =
    """
      |{
      |    "id": 17922525,
      |    "repository": {
      |        "id": 1825929,
      |        "name": "hall-hooks",
      |        "owner_name": "drdamour",
      |        "url": "https://github.com/drdamour/hall-hooks"
      |    },
      |    "number": "13",
      |    "config": {
      |        "language": "scala",
      |        "scala": [
      |            "2.10.3"
      |        ],
      |        "deploy": {
      |            "provider": "heroku",
      |            "api_key": {
      |                "secure": "hUxppngfpBsAl0jafG0GF4Z6O92spf0GraQ7rc68VJUC6qS4anXNMu6mO2Bu4qGtoCm4Z+LE="
      |            },
      |            "app": "hall-hooks",
      |            "true": {
      |                "repo": "drdamour/hall-hooks"
      |            }
      |        },
      |        "notifications": {
      |            "webhooks": [
      |                "https://hall-hooks.herokuapp.com/travis-ci/buildnotification/"
      |            ]
      |        },
      |        ".result": "configured"
      |    },
      |    "status": 0,
      |    "result": 0,
      |    "status_message": "Passed",
      |    "result_message": "Passed",
      |    "started_at": "2014-01-30T17:52:32Z",
      |    "finished_at": "2014-01-30T18:09:02Z",
      |    "duration": 990,
      |    "build_url": "https://travis-ci.org/drdamour/hall-hooks/builds/17922525",
      |    "commit": "89df55e2cee1393d08790a291a59ee1055ed3547",
      |    "branch": "master",
      |    "message": "Fixed examples",
      |    "compare_url": "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1",
      |    "committed_at": "2014-01-30T17:49:37Z",
      |    "author_name": "drdamour",
      |    "author_email": "drdamour@gmail.com",
      |    "committer_name": "drdamour",
      |    "committer_email": "drdamour@gmail.com",
      |    "matrix": [
      |        {
      |            "id": 17922526,
      |            "repository_id": 1825929,
      |            "parent_id": 17922525,
      |            "number": "13.1",
      |            "state": "finished",
      |            "config": {
      |                "language": "scala",
      |                "scala": "2.10.3",
      |                "notifications": {
      |                    "webhooks": [
      |                        "https://hall-hooks.herokuapp.com/travis-ci/buildnotification/"
      |                    ]
      |                },
      |                ".result": "configured",
      |                "addons": {}
      |            },
      |            "status": null,
      |            "result": null,
      |            "commit": "89df55e2cee1393d08790a291a59ee1055ed3547",
      |            "branch": "master",
      |            "message": "Fixed examples",
      |            "compare_url": "https://github.com/drdamour/hall-hooks/compare/544ce058621d...89df55e2cee1",
      |            "committed_at": "2014-01-30T17:49:37Z",
      |            "author_name": "drdamour",
      |            "author_email": "drdamour@gmail.com",
      |            "committer_name": "drdamour",
      |            "committer_email": "drdamour@gmail.com",
      |            "finished_at": "2014-01-30T18:09:02Z"
      |        }
      |    ],
      |    "type": "push"
      |}
    """.stripMargin

  val travisSimulationForm = Form(
    mapping(
      "roomToken" -> nonEmptyText,
      "payload" -> nonEmptyText
    )(TravisSimulation.apply)(TravisSimulation.unapply)
  )

  def index = Action { implicit request =>
    Ok(views.html.Travis.info(travisSimulationForm.fill(TravisSimulation("", examplePayload))))
  }

  def runSimulation = Action.async { implicit request =>
    travisSimulationForm.bindFromRequest().fold(
      badForm => {
        Future(BadRequest(views.html.Travis.info(badForm)))
      },
      validSimulation => {
        doStuff(validSimulation.roomToken, validSimulation.payload)
      }

    )
  }
}

object TravisController extends Controller with TravisController with HallCommandHandlerSlice