package models

/**
 * Created with IntelliJ IDEA.
 * User: mario
 * Date: 20.01.13
 * Time: 17:01
 * To change this template use File | Settings | File Templates.
 */
case class Verein(name: String) {
  var arbeitsstdProJahr = 2.0D
  var vergütungVorstand = 50.00
  var vergütungVorsitzender = 100.00

  var kontonr = "355 100 18 03"
  var blz     = "870 500 00"
  var bank    = "Sparkasse Chemnitz"

  var zahlungszielTage = 21
}
