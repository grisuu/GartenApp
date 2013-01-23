package controllers

import play.api.mvc._
/**
 * controllers 2013
 *
 * User: PathTracer
 * Date: 09.01.13
 * Time: 21:52
 */
object Menu extends Controller {
  val menuItems: List[(String,String)] = List(
    ("/", "Home"),
    ("/mitglieder", "Mitglieder"),
    ("/gaerten", "Gärten"),
    ("/pachtvertraege", "Pachtverträge"),
    ("/abrechnungen", "Abrechnungen")
  )

}
