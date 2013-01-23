package models

/**
 * gartenapp2.apps.garten.model 2013
 *
 * User: PathTracer
 * Date: 04.01.13
 * Time: 22:20
 */
case class Wasserzaehler(nr: Int) extends Zaehler {
  var ablesungen: List[Ablesung] = List()
  val einheit = "m³"
}
