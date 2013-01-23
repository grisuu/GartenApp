package models

/**
 * Created with IntelliJ IDEA.
 * User: mario
 * Date: 14.01.13
 * Time: 23:57
 * To change this template use File | Settings | File Templates.
 */
case class AbrechnungsDaten(jahr: Int, mitgliederList: List[Int], umlage: Double, beitrag: Double, strom: Double, wasser: Double, pauschal: Double) {

}
