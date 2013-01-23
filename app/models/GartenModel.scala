package models

import collection.mutable.HashMap

/**
 * gartenapp2.apps.garten 2012
 *
 * User: PathTracer
 * Date: 18.12.12
 * Time: 13:19
 */
/*object GartenModel extends Serializable {
  private var lastGartenNr: Int = 0
  def getNewGartentNr: Int = { lastGartenNr += 1; lastGartenNr }
}*/


class GartenModel extends Serializable {
  var list: HashMap[Int, Garten] = HashMap()

  def apply() = list
  def apply(nr: Int) = list(nr)

  def size = list.size

  def nrs = list.keys
  def values = list.values

}
