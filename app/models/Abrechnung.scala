package models

import org.joda.time.DateTime
import controllers.VereinsController

/**
 * Created with IntelliJ IDEA.
 * User: mario
 * Date: 13.01.13
 * Time: 17:06
 * To change this template use File | Settings | File Templates.
 */
object Abrechnung extends Serializable {
  var lastAbrNr: Int = 1
}

case class Abrechnung(nr: Int, pachtvertrag: Pachtvertrag) {
  val datum: DateTime = new DateTime()
  val fällig: DateTime = datum.plusDays(VereinsController.verein.zahlungszielTage)
  var positionen: List[Position] = List()

  def sum: Double = positionen.foldLeft(0.0)((ges, p) => ges + p.sum)
}
