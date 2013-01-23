package models

/**
 * Created with IntelliJ IDEA.
 * User: mario
 * Date: 14.01.13
 * Time: 00:28
 * To change this template use File | Settings | File Templates.
 */
case class Jahresabrechnung(jahr: Int, abrechnungen: Map[Int, Abrechnung]) {
  def sum = abrechnungen.values.foldLeft(0.0)(_+_.sum)
}
