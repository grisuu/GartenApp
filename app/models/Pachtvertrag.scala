package models

import org.joda.time.DateTime

/**
 * gartenapp2.apps.common 2012
 *
 * User: PathTracer
 * Date: 18.12.12
 * Time: 22:45
 */
case class Pachtvertrag(nr: Int, begin: DateTime, mitglied: Mitglied, garten: Garten) {

  override def toString = "Pachtvertrag(%d, %s, %s, %s)" format (nr, mitglied, garten, begin.toString("dd.MM.yy"))
}