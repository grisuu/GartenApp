package controllers

import java.io.{IOException, FileInputStream, File}
import java.text.{NumberFormat, ParseException, DecimalFormat}
import org.joda.time.DateTime
import play.api._
import mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formatter
import models._
import scala.Some
import views.html
import models.Garten
import models.Logger
import models.Ablesung
import models.Garten
import java.util.Locale
import tools.fileio.CsvReader

/**
 * gartenapp2.apps.garten.controller 2012
 *
 * User: PathTracer
 * Date: 18.12.12
 * Time: 13:18
 */
object GartenController extends Controller {

  val gärten: GartenModel = MainController.mainModel.gartModel

  val gartenForm: Form[Garten] = Form(
    mapping(
      "nr"      -> number.verifying("ein Garten mit dieser Nummer ist schon vorhanden", !exists(_)),
      "fläche"  -> nonEmptyText.transform(_.toDouble, (f: Double) => f.toString).verifying("muss ein positiver Wert sein", _ >= 0),
      "laube"   -> nonEmptyText.transform(_.toDouble, (f: Double) => f.toString).verifying("muss ein positiver Wert sein", _ >= 0),
      "grundB"  -> nonEmptyText.transform(_.toDouble, (f: Double) => f.toString).verifying("muss ein positiver Wert sein", _ >= 0)
    )(Garten.apply)(Garten.unapply)
  )

  implicit def doubleFormat = new Formatter[Double] {
    def bind(key: String, data: Map[String, String]) = {
      val df = NumberFormat.getInstance(Locale.GERMAN)
      val decZahl = df.parse(data(key))
      Right(decZahl.doubleValue())
    }
    def unbind(key: String, value: Double) = Map(key -> value.toString)
  }

  val ablFrom: Form[Ablesung] = Form(
    mapping(
      "datum"   -> date("yyyy-MM-dd").transform[DateTime](new DateTime(_), _.toDate),
      "wert"    -> of[Double]
    )(Ablesung.apply)(Ablesung.unapply)
  )

  def getGartenList(sel: Int = -1) = Action { implicit request =>
    Ok(views.html.gaerten(gärten.list.values.toList.sortBy(_.nr), gärten.list.get(sel)))
  }

  def showGarten(gNr: Int) = Action {
    gärten.list.get(gNr).map { g =>
      Ok(views.html.garten_form(gartenForm.fill(g)))
    }.getOrElse(NotFound("nicht gefunden"))
  }

  def newGarten = Action {
    Ok(views.html.garten_form(gartenForm))
  }

  /**
   * Prüft, ob ein Garten mit der Nr. existiert
   * @param nr Gartennr.
   * @return Boolean
   */
  def exists(nr: Int): Boolean = gärten.list.isDefinedAt(nr)

  def processNewGarten = Action { implicit request =>
    gartenForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(html.garten_form(formWithErrors))
      }, {
        case g: Garten =>
          if (addGarten(g))
            Redirect("/gaerten").flashing("info" -> "Garten hinzugefügt")
          else
            Redirect("/gaerten").flashing("error" -> ("Garten Nr. %d ist schon vorhanden" format g.nr))
        case e => println(e); BadRequest("geht ne")
      }
    )
  }

  def addGarten(g: Garten): Boolean = {
    if (!exists(g.nr)) {
      gärten.list += g.nr -> g
      true
    } else {
      false
    }
  }

  /**
   * Fügt einen neuen Garten hinzu und gibt den neu hinzugefügten zurück.
   * Wenn der Garten schon existiert wird None zurck gegeben
   * @param nr Gartennr.
   * @param fläche Gartenfläche in m²
   * @param laube Fläche der Laube in m²
   * @param grundB Grundsteuer B, wenn keine dann 0,00 (in EUR)
   * @return Some(neuer Garten) oder None
   */
  def addGarten(nr: Int, fläche: Double, laube: Double, grundB: Double = 0.0): Option[Garten] = {
    if (!exists(nr)) {
      val newGarten = Garten(nr, fläche, laube, grundB)
      gärten.list += nr -> newGarten
      Logger.add("Garten Nr. %d wurde hinzugefügt" format nr)

      Some(newGarten)
    } else {
      Logger.add("Garten Nr. %d existiert schon" format nr)
      None
    }
  }

  def gartenDetails(nr: Int) = Action {
    gärten.list.get(nr).map { g =>
      Ok(views.html.garten_details(g, ablFrom))
    }.getOrElse(NotFound("nichts gefunden"))
  }
/*
  def gartenInUse(garten: Garten) = {
    val pc = new PachtvertragController
    pc.pachtverträge.list.collectFirst {
      case (nr, pa) if pa.garten == garten => pa
    }
  }
*/

  def importAblesungen = Action {
    val abl07 = importAblesungenFromFile(new DateTime("2007-01-01"), new java.io.File("public/abl_2007.csv"))
    val abl08 = importAblesungenFromFile(new DateTime("2008-01-01"), new java.io.File("public/abl_2008.csv"))
    val abl09 = importAblesungenFromFile(new DateTime("2009-01-01"), new java.io.File("public/abl_2009.csv"))
    val abl10 = importAblesungenFromFile(new DateTime("2010-01-01"), new java.io.File("public/abl_2010.csv"))
    val abl11 = importAblesungenFromFile(new DateTime("2011-01-01"), new java.io.File("public/abl_2011.csv"))
    if (new java.io.File("public/abl_2012.csv").exists())
      importAblesungenFromFile(new DateTime("2012-01-01"), new java.io.File("public/abl_2012.csv"))

    if (abl07 && abl08 && abl09 && abl10 && abl11)
      Redirect(routes.GartenController.getGartenList(-1))
    else
      BadRequest
  }

  def importAblesungenFromFile(importDate: DateTime, f: File): Boolean = {
    if (f.canRead) {

      val csvi = new CsvReader(f)
      val abl = csvi.parseVerbrauch(importDate)

      gärten.list.forall {
        case (nr, garten) if abl.get(nr).isDefined =>
          addAblesung(gärten.list(nr).stromzähler, Ablesung(importDate, abl(nr)._1))
          addAblesung(gärten.list(nr).wasseruhr, Ablesung(importDate, abl(nr)._2))
          true
        case (nr, garten) if abl.get(nr).isEmpty =>
          Logger.add("keine Ablesung für Garten Nr. %d gefunden!" format nr)
          //throw AblesungNotFoundException(garten)
          true
        case _ => false
      }

    } else {
      false
    }
  }

  def processNewAblesung(nr: Int, z: String) = Action { implicit request =>
    ablFrom.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.gaerten(gärten.list.values.toList.sortBy(_.nr)))
      }, {
        case abl: Ablesung =>
          val zähler = z match {
            case "strom"  => gärten(nr).stromzähler
            case "wasser" => gärten(nr).wasseruhr
          }
          addAblesung(zähler, abl)
          Redirect(routes.GartenController.getGartenList(nr)).flashing("info" -> "neuer Ablesewert hinzugefügt")
        case _ => BadRequest("")
      }
    )
  }

  def addAblesung(zähler: Zähler, abl: Ablesung) = {
    zähler.ablesungen ::= abl
    Logger.add("neue Ablesung %s für Zähler %s" format (abl, zähler))
  }

  def delAblesung(zähler: Zähler, abl: Ablesung) = {
    zähler.ablesungen = zähler.ablesungen.filterNot(_ eq abl)
    Logger.add("Ablesung %s für Zähler %s wurde gelöscht" format (abl, zähler))
  }

}

case class AblesungNotFoundException(garten: Garten) extends Exception