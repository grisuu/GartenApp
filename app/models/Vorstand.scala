package models

import org.joda.time.{DateMidnight, DateTime}

/**
 * gartenapp2.apps.common 2012
 *
 * User: PathTracer
 * Date: 23.12.12
 * Time: 16:49
 */
class Vorstand(nr: Int, anrede: Sex.Value, vorname: String, nachname: String, adresse: Adresse, createDate: DateTime, dob: DateMidnight = new DateMidnight())
  extends Mitglied(nr, anrede, vorname, nachname, adresse, createDate, dob) {

}
