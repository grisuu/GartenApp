package models

/**
 * gartenapp2.apps.garten.model 2013
 *
 * User: PathTracer
 * Date: 05.01.13
 * Time: 18:02
 */
case class Stromzähler(nr: Int) extends Zähler {
  var ablesungen: List[Ablesung] = List()
  val einheit = "kWh"
}
