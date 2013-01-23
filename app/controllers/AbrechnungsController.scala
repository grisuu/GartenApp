package controllers

import java.text.NumberFormat
import org.joda.time.{Days, DateTime}
import play.api._
import libs.json.Json
import mvc._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formatter
import models._
import java.util.Locale
import anorm.toParameterValue
import tools.helper.Formatta.doubleFormat
import collection.mutable
import java.io.{FileInputStream, FileOutputStream, File}

/**
 * gartenapp2.apps.garten.controller 2012
 *
 * User: PathTracer
 * Date: 18.12.12
 * Time: 13:18
 */
object AbrechnungsController extends Controller {

  var abrechnungen: AbrechnungsModel = MainController.mainModel.abrModel

  val heute = new DateTime()

  def getNewAbrNr: Int = { Abrechnung.lastAbrNr = Abrechnung.lastAbrNr + 1; Abrechnung.lastAbrNr }

  case class Joar(name: String)

  def saveTest = Action {
    val f = new File("data/test.dat")
    val fo = new FileOutputStream(f)

    println(Joar.hashCode())

    fo.write(scala.util.Marshal.dump(new Joar("test")))
    fo.close()

    val fi = new FileInputStream(f)
    val bytes = Stream.continually(fi.read).takeWhile(-1 !=).map(_.toByte).toArray
    val cl = scala.util.Marshal.load(bytes)

    Ok("ogg " + cl)
  }

  val abrForm: Form[AbrechnungsDaten] = Form(
    mapping(
      "jahr"      -> number(min = 1900, max = heute.getYear),
      "pacht"     -> list(number).verifying("letzte Ablesung der Zählerstände zu alt", _.forall {
        nr => val strAbl = PachtvertragController.pachtverträge(nr).garten.stromzähler.ablesungen.headOption
              val wasAbl = PachtvertragController.pachtverträge(nr).garten.wasseruhr.ablesungen.headOption
              println(Days.daysBetween(strAbl.get.datum, heute).getDays)
              (strAbl.isDefined && wasAbl.isDefined &&
               Days.daysBetween(strAbl.get.datum, heute).getDays <= 6000 &&
               Days.daysBetween(wasAbl.get.datum, heute).getDays <= 6000)
      }),
      "umlage"    -> of[Double],
      "beitrag"   -> of[Double],
      "strom"     -> of[Double],
      "wasser"    -> of[Double],
      "pauschale" -> of[Double]

    )(AbrechnungsDaten.apply)(AbrechnungsDaten.unapply)
  )

  def getAbrechnungsList(jahr: Int, nr: Int = -1) = Action { implicit request =>
    Ok(views.html.abrechnungen(abrechnungen.list, None))
  }

  def newAbrechnung = Action {
    Ok(views.html.abrechnung_new(abrForm))
  }

  def processNewAbrechung = Action { implicit request =>
    abrForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(views.html.abrechnung_new(formWithErrors))
      }, {
        case abr: AbrechnungsDaten =>
          try {
            processNewJahresabrechnung(abr)
            Ok(views.html.abrechnungen(abrechnungen.list))
          } catch {
            case e: Exception => BadRequest(views.html.abrechnung_new(abrForm)).flashing("error" -> e.getMessage)
          }


      }
    )
  }

  def deepCopy[A](a: A)(implicit m: reflect.Manifest[A]): A =
    util.Marshal.load[A](util.Marshal.dump(a))

  def processNewJahresabrechnung(abrData: AbrechnungsDaten) = {

    /*if (abrechnungen.list.get(abrData.jahr).forall(_.abrechnungen.exists(a => abrData.mitgliederList.contains(a._2.pachtvertrag.mitglied.nr))))
      throw new Exception("Abrechnung für das Jahr %d für Mitglied %s schon vorhanden!") //TODO: neue Exception*/

    val jabr =
      PachtvertragController.pachtverträge.list.filterKeys(abrData.mitgliederList.contains(_)).map {
        case (nr, pv) =>
          val abr = new Abrechnung(getNewAbrNr, pv.copy())
          val pachtProQmeter = 0.14
          val wasserAns = 64.47
          val stromAns = 64.47
          val anzMitgl = PachtvertragController.pachtverträge.size
          val zählerPP = (wasserAns + stromAns) / anzMitgl

          val gesStromVerbrauch = PachtvertragController.pachtverträge.list.foldLeft(0.0)((l, pv) => l + pv._2.garten.stromzähler.lastVerbrauch)
          val gesWasserVerbrauch = PachtvertragController.pachtverträge.list.foldLeft(0.0)((l, pv) => l + pv._2.garten.wasseruhr.lastVerbrauch)

          val stromPreis = abrData.strom / gesStromVerbrauch
          val wasserPreis = abrData.wasser / gesWasserVerbrauch

//
          val strAbl = pv.garten.stromzähler.ablesungen.headOption match {
            case Some(a:Ablesung) => a
            case None => throw new Exception("kein Stromzählerstand")
          }
          val wasAbl = pv.garten.wasseruhr.ablesungen.headOption match {
            case Some(a:Ablesung) => a
            case None => throw new Exception("kein Wasserzählerstand")
          }

          abr.positionen = List(Position("Vereinsbeitrag*", 1, "x", abrData.umlage),
                                Position("Pachtkosten", pv.garten.fläche, "m²", pachtProQmeter),
                                Position("Stromverbrauch", pv.garten.stromzähler.verbrauch(strAbl), pv.garten.stromzähler.einheit, stromPreis),
                                Position("Wasserverbrauch", pv.garten.wasseruhr.verbrauch(wasAbl), pv.garten.wasseruhr.einheit, wasserPreis),
                                Position("Wasser/Strom Unterzählergebühr", 1, "x", zählerPP),
                                Position("Grundsteuer B", 1, "x", pv.garten.grundB),
                                Position("Mitgliederbeitrag an den Stadtverband", 1, "x", abrData.beitrag)
                                )
          //Vorstand
          pv.mitglied.position match {
            case MitgliedsTyp.VORSITZENDER => abr.positionen = Position("Aufwandsentschädigung Vorstand", 1, "x", -100.00) :: abr.positionen
            case MitgliedsTyp.VORSTAND => abr.positionen = Position("Aufwandsentschädigung Vorstand", 1, "x", -50.00) :: abr.positionen
            case MitgliedsTyp.MITGLIED =>
              val arbStd = pv.mitglied.arbeitsStunden.foldLeft(VereinsController.verein.arbeitsstdProJahr)(_-_.dauer)
              abr.positionen = Position("nicht geleistete Arbeitsstunden", if (arbStd < 0) 0 else arbStd , "Std.", 8.00) :: abr.positionen
            case _=>
          }

         (abr.nr -> abr)
      }.toMap
    abrechnungen.list += (abrData.jahr -> Jahresabrechnung(abrData.jahr, jabr))
  }

  def printAbrechnung(jahr: Int, nr: Int) = Action {
    Ok(views.html.abrechnung_print(jahr, abrechnungen(jahr).abrechnungen(nr), VereinsController.verein ))
  }

  def addAbrechnung(abrechnung: Jahresabrechnung) = {
    abrechnungen.list = mutable.HashMap(abrechnung.jahr -> abrechnung) ++ abrechnungen.list

  }


}