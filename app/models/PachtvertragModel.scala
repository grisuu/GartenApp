package models

import collection.mutable.HashMap

/**
 * gartenapp2.apps.pachtvertrag.model 2012
 *
 * User: PathTracer
 * Date: 18.12.12
 * Time: 22:42
 */
class PachtvertragModel() extends Serializable {
  var list: HashMap[Int, Pachtvertrag] = HashMap()
  var lastPachtNr: Int = 0


  def apply(nr: Int) = list(nr)

  def size = list.size

  def values = list.values
}
