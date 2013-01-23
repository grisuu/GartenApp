package models

import scala.collection.mutable.HashMap

/**
 * Created with IntelliJ IDEA.
 * User: PathTracer
 * Date: 03.12.12
 * Time: 23:16
 */

class AbrechnungsModel extends Serializable {
  var list: HashMap[Int, Jahresabrechnung] = HashMap()
  var lastAbrechnNr = 0

  def apply() = list
  def apply(nr: Int) = list(nr)

  def size = list.size

  def nrs = list.keys
  def values = list.values
}
