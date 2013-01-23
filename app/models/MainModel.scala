package models

/**
 * gartenapp2.apps.main.model 2012
 *
 * User: PathTracer
 * Date: 19.12.12
 * Time: 17:03
 */
class MainModel extends Serializable {
  val custModel  = new CustomersModel
  val gartModel  = new GartenModel
  val pachtModel = new PachtvertragModel
  val abrModel   = new AbrechnungsModel
}
