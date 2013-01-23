package models

/**
 * Created with IntelliJ IDEA.
 * User: mario
 * Date: 13.01.13
 * Time: 17:07
 * To change this template use File | Settings | File Templates.
 */
case class Position(text: String, menge: Double, einheit: String, wert: Double) {
  def sum = menge * wert

  override def toString() = "Pos(%s: %.2f %s %.2f = %.2f" format (text, menge, einheit, wert, sum)
}
