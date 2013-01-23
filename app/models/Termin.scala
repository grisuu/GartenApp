package models

import org.joda.time.ReadableDateTime

/**
 * gartenapp2.apps.common.model 2013
 *
 * User: PathTracer
 * Date: 06.01.13
 * Time: 13:54
 */
case class Termin(datum: ReadableDateTime, titel: String, text: String = "", ref: Option[AnyRef] = None) {
  def toFmtStr = "%s Uhr - %s\n%s" format (datum.toString("HH:mm"), titel, text)
}
