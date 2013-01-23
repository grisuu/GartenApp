package tools.os

import java.net.{URISyntaxException, URI}
import java.io.File
import java.awt.Desktop

/**
 * gartenapp2.tools.fileio 2012
 *
 * User: PathTracer
 * Date: 18.12.12
 * Time: 11:05
 */
object Desk {
  val desk = if (Desktop.isDesktopSupported) Some(Desktop.getDesktop) else None

  def openBrowser(str: String) {
    openBrowser(new URI(str))
  }

  def openBrowser(file: File) {
    openBrowser(file.toURI)
  }

  def openBrowser(path: URI) {
    if (desk.isDefined && desk.get.isSupported(Desktop.Action.BROWSE)) {
      try {
        desk.get.browse(path)
      } catch {
        case e: URISyntaxException => println("URL fehlerhaft")
        case _: Exception => println("kann Browser nicht öffnen")
      }
    } else {
      //Dialog.showMessage(null, "Browser nicht unterstützt", "Fehler beim öffnen der Seite", Dialog.Message.Error)
    }
  }

  def sendMail(to: String) {
    sendMail(new URI("mailto:" + to))
  }

  def sendMail(toUri: URI) {
    if (desk.isDefined && desk.get.isSupported(Desktop.Action.MAIL)) {
      try {
        desk.get.mail(toUri)
      } catch {
        case e: URISyntaxException => println("eMail-Adresse fehlerhaft")
        case _: Exception => println("kann eMail nicht öffnen nicht öffnen")
      }
    } else {
      //Dialog.showMessage(null, "eMail nicht unterstützt", "Fehler beim öffnen des eMail-Programms", Dialog.Message.Error)
    }
  }
}
