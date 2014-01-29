package controllers

import play.api._
import play.api.mvc._

object HomeController extends Controller {
  def index = Action { implicit request =>
    Ok(views.html.index())
  }
}
