package models

/**
 * gartenapp2.apps.garten.model 2013
 *
 * User: PathTracer
 * Date: 04.01.13
 * Time: 22:20
 */
case class Wasserzähler(nr: Int) extends Zähler {
  var ablesungen: List[Ablesung] = List()
  val einheit = "m³"
}
