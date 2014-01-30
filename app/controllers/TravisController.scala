package controllers

import play.api.mvc._
import hall.HallCommandHandlerSlice
import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.Json

case class TravisTestFormData(request:String)

trait TravisController {
  this:Controller with HallCommandHandlerSlice =>

  def sendBuildStatusToHall = Action { implicit request =>
    //Get the json from the form body
    val json = Json.parse(request.body.asFormUrlEncoded.get("payload").head)

    Ok("")
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

  val travisPayloadForm = Form(
    mapping(
      "payload" -> nonEmptyText
    )(TravisTestFormData.apply)(TravisTestFormData.unapply)
  )

  def index = Action { implicit request =>
    Ok(views.html.Travis.info(travisPayloadForm.fill(TravisTestFormData(examplePayload))))
  }

}

object TravisController extends Controller with TravisController with HallCommandHandlerSlice
