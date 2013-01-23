package models

import util.Random

/**
 * gartenapp2.apps.common 2012
 *
 * User: PathTracer
 * Date: 17.12.12
 * Time: 00:05
 */
case class Garten(nr: Int, var fläche: Double, var laube: Double, var grundB: Double = 0.0) {

  val wasseruhr   = new Wasserzähler(Random.nextInt(Int.MaxValue))
  val stromzähler = new Stromzähler(Random.nextInt(Int.MaxValue))

  override def toString = "Garten Nr. %3d" format nr
}
