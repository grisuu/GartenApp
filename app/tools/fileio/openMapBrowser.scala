package tools.fileio

import java.net.URLEncoder
import models.Adresse
import tools.os.Desk

/**
 * gartenapp2.tools.fileio 2012
 *
 * User: PathTracer
 * Date: 18.12.12
 * Time: 11:44
 */
class openMapBrowser(addresse: Adresse) {
  val url = "http://maps.google.de/maps?q=%s&hl=de" format URLEncoder.encode(addresse toString, "UTF-8")
  val b = Desk.openBrowser(url)
}
