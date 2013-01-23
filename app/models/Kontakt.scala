package models

/**
 * Created with IntelliJ IDEA.
 * User: PathTracer
 * Date: 02.12.12
 * Time: 21:52
 * To change this template use File | Settings | File Templates.
 */

object KontaktType extends Enumeration {
  val Tel       = Value("Telefon")
  val Fax       = Value("Telefax")
  val Mobile    = Value("Mobil")
  val Email     = Value("Email")
  val Internet  = Value("Internet")
}

case class Kontakt(name: String, wert: String, typ: KontaktType.Value) {

}
