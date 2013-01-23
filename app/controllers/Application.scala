package controllers

import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._


import views._

import play.api.mvc.Controller
import tools.fileio.{ObjectSaver}

/**
 * controllers 2013
 *
 * User: PathTracer
 * Date: 08.01.13
 * Time: 21:49
 */
object Application extends Controller {

  MainController.startup()

  /**
   * Homepage
   */
  def index = Action {
    Ok(html.index(MainController.appName))
  }

}
