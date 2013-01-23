package models

import tools.helper.Formatta

/**
 * gartenapp2.apps.garten.model 2013
 *
 * User: PathTracer
 * Date: 04.01.13
 * Time: 23:49
 */
abstract class Zaehler {
  val nr: Int
  var ablesungen: List[Ablesung]
  val einheit: String

  def verbrauch(a: Ablesung): Double = {
    Formatta.round(ablesungen.sortWith(_.datum isBefore _.datum).foldLeft(0.0) {
      case (wert, abl) if abl eq a =>
        if ((a.wert - wert) >= 0)
          return Formatta.round(a.wert - wert, 2)
        else
          return Formatta.round(abl.wert, 2)
      case (wert, abl) if abl ne a => abl.wert
    }, 2)
  }

  def lastVerbrauch: Double = ablesungen.headOption.map(verbrauch(_)).getOrElse(0.0)
}
