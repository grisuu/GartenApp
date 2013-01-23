package models

/**
 * gartenapp2.apps.common 2012
 *
 * User: PathTracer
 * Date: 18.12.12
 * Time: 11:46
 */
case class Adresse(str: String, hausnr: String, plz: String, ort: String, land: String = "Deutschland") {
  val toHtml = if (str.nonEmpty && plz.nonEmpty && ort.nonEmpty) "%s %s,<br />%s %s<br />%s" format (str, hausnr, plz, ort, land) else "keine"
  override def toString = "%s %s, %s %s %s" format (str, hausnr, plz, ort, land)
}
