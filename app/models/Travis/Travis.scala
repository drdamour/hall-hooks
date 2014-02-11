package models.Travis

import play.api.libs.json.Json

//These are modeled after the example at from https://gist.github.com/svenfuchs/1225015

case class Repository(
  id:Int,
  name:String,
  owner_name:String,
  url:String
)

object Repository {
  implicit val repositoryFormat = Json.format[Repository]
}

case class BuildMessage(
  id: Int,
  number: String,
  status: Int, //what is this?
  started_at: Option[String], //this seems wrong
  finished_at: Option[String], //this seems wrong,
  status_message: String,
  commit: String,
  branch: String,
  message: String,
  compare_url: String,
  committed_at: String, //TODO change to time
  committer_name: String,
  committer_email: String,
  author_name: String,
  author_email: String,
  build_url: String,
  repository: Repository,
  pull_request_number: Option[Int]

)

object BuildMessage {
  //Note this has to be after the Repository companion object as this needs the implicit defined in there
  implicit val payloadFormat = Json.format[BuildMessage]
}



