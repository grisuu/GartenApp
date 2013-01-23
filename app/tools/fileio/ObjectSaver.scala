package tools.fileio

import java.io.{FileOutputStream, IOException, FileInputStream, File}
import models._
import scala.Serializable
import com.sun.xml.internal.ws.encoding.soap.SerializationException
import play.api.libs.json._
import controllers.MitgliederController

/**
 * Created with IntelliJ IDEA.
 * User: PathTracer
 * Date: 10.12.12
 * Time: 21:45
 */
class ObjectSaver {
  /**
   * Läd das GartenModel aus der angegebenen Datei und gibt dieses zurück
   * wenn ein Fehler auftritt wird ein leeres GartenModel erzeugt
   *
   * @param f Datei zum gespeicherten GartenModel-Objekt
   * @return GartenModel
   */
  def loadFromFile[T <: Serializable](f: File)(implicit m: scala.reflect.Manifest[T]): T = {
    if (f.exists() && f.canRead) {
      val in = new FileInputStream(f)

      try {
        val bytes = Stream.continually(in.read).takeWhile(-1 !=).map(_.toByte).toArray
        scala.util.Marshal.load[T](bytes)
      } catch {
        case c: ClassNotFoundException => Logger.add("Klasse nicht gefunden " + c)
          m.erasure.newInstance().asInstanceOf[T]
        case e: IOException =>  Logger.add("Fehler beim Laden von %s: %s " format (m.erasure.getCanonicalName, e.getMessage))
          m.erasure.newInstance().asInstanceOf[T]
        case o:Exception => Logger.add("other Exception")
          m.erasure.newInstance().asInstanceOf[T]
      } finally
        in close()

    } else {
      Logger.add("Datei für %s nicht gefunden oder nicht lesbar" format m.erasure.getCanonicalName)
      m.erasure.newInstance().asInstanceOf[T]
    }
  }

  def saveToFile[T <: Serializable](f: File, obj: T)(implicit m: scala.reflect.Manifest[T]): Boolean = {
    //Save to File
    val out = new FileOutputStream(f)
    out.write(scala.util.Marshal.dump[T](obj))
    try {
      Logger.add("\"%s\" wurde in der Datei \"%s\" gespeichert" format (obj.getClass.getName, f.getAbsoluteFile))
      true
    } catch {
      case e: SerializationException => Logger.add("Serialisierungsfehler " + e.getMessage); false
      case e: IOException => Logger.add("IO- Error " + e.getMessage); false
      case e: Exception => Logger.add("other Error " + e.getLocalizedMessage); false
    } finally {
      out close()
    }

  }

}
