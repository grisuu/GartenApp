package controllers

import org.joda.time.{DateMidnight, DateTime}
import models._
import play.api.mvc._
import views.html
import play.api.data.Form
import play.api.data.Forms._
import tools.helper.Formatta.doubleFormat
import tools.fileio.CsvReader
import play.api.libs.concurrent.{Promise, Akka}
import play.api.Play.current
import play.cache.Cache

object MitgliederController extends Controller {
//  Sex.values.toMap[String,String]
  val customer: CustomersModel = MainController.mainModel.custModel
  var currMitglied: Option[Mitglied] = None
  val mitgliederForm: Form[Mitglied] = Form(
    mapping(
      "nr"          -> number,
      "anrede"      -> nonEmptyText.transform(Sex.withName(_),(s: Sex.Value) => s.toString),
      "vorname"     -> nonEmptyText,
      "nachname"    -> nonEmptyText,
      "adresse"     -> mapping(
        "strasse"     -> nonEmptyText,
        "hausnr"      -> nonEmptyText,
        "plz"         -> nonEmptyText,
        "ort"         -> nonEmptyText,
        "land"        -> text
      )(Adresse.apply)(Adresse.unapply),
      "create"      -> ignored(new DateTime),
      "dob"         -> date("yyyy-MM-dd").transform(new DateMidnight(_), (d: DateMidnight) => d.toDate),
      "typ"         -> number.transform(MitgliedsTyp(_),(s: MitgliedsTyp.Value) => s.id)
    )(Mitglied.apply)(Mitglied.unapply)
  )

  val arbeitsstdForm: Form[Arbeitsstunde] = Form(
    mapping(
      "arbstd"      -> of[Double].verifying("keine oder ungültige Zahl eingegeben",_ > 0),
      "arbstdtext"  -> text
    )(Arbeitsstunde.apply)(Arbeitsstunde.unapply)
  )

  /**
    * neue Mitgliedernr. erzeugen und Zähler um 1 erhöhen
   * @return neue Mitgliedernr.
   */
  private def getNewCustomersNr = {
    customer.lastCustNr += 1
    customer.lastCustNr
  }

  private def getCustList = customer.values.toList.sortBy(_.nr)

  def getMitgliederListe(nr: Int = 0) = Action { implicit request =>
    val mitgliederList = Mitglied.all()
    Ok(views.html.mitglieder(mitgliederList, mitgliederList.collectFirst {
      case m: Mitglied if m.nr == nr => m
    }))
  }

  /**
   * fügt einen neues Mitglied mit neuer Nr zum Model hinzu und sendet Event zum Aktualisieren der Anzeige
   *
   * @param vorname Vorname
   * @param nachname Nachname
   * @param str Strasse
   * @param hausnr Straße und Hausnr
   * @param plz PLZ
   * @param ort Wohnort
   * @param dob Geburtsdatum
   */
  def addMitglied(anrede: Sex.Value, vorname: String, nachname: String, str: String, hausnr: String, plz: String, ort: String, dob: DateMidnight = new DateMidnight(), isVorstand: Boolean = false): Mitglied = {
    val newNr = getNewCustomersNr
    val adresse = Adresse(str, hausnr, plz, ort, "Deutschland")

    val newCustomer = if (isVorstand) new Vorstand(newNr, anrede, vorname, nachname, adresse, new DateTime, dob) else Mitglied(newNr, anrede, vorname, nachname, adresse, new DateTime, dob)

    Mitglied.create(newCustomer)
    Logger.add("%s Nr. %s hinzugefügt" format (newCustomer.getClass.getCanonicalName, newNr))

    newCustomer
  }

  def newMitglied = Action {
    Ok(views.html.mitglieder_new(mitgliederForm))
  }

  /**
   * fügt einen neues Mitglied mit neuer Nr zum Model hinzu und sendet Event zum Aktualisieren der Anzeige
   *
   */
  def addMitglied = Action { implicit request =>
    mitgliederForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(html.mitglieder_new(formWithErrors)) },
      { case m: Mitglied =>
          println(Mitglied.create(m))
          Redirect(routes.MitgliederController.getMitgliederListe(m.nr)).flashing("info" -> "Mitglied hinzugefügt")
        case e => println(e); BadRequest("geht ne")
      }

    )
  }

  def saveMitglied = Action { implicit request =>
    mitgliederForm.bindFromRequest.fold(
      formWithErrors => {
        println(formWithErrors)
        BadRequest(views.html.mitglieder_new(formWithErrors))
      }, {
        case neu: Mitglied =>
          Mitglied.update(neu)
          Redirect(routes.MitgliederController.getMitgliederListe(neu.nr)).flashing("info" -> "Mitglied wurde geändert")
        case e => println(e); BadRequest("geht ne")
      }
    )
  }

  def showCustomer(mId: Int) = Action {
    customer.list.get(mId).map { m =>
      Ok(views.html.mitglieder_new(mitgliederForm.fill(m)))
    }.getOrElse(NotFound("nicht gefunden"))
  }

  def editCustomer(mId: Int) = Action {
    Mitglied.find(mId).map { m =>
      Ok(views.html.mitglieder_form(mitgliederForm.fill(m)))
    }.getOrElse(NotFound("nicht gefunden"))
  }

  /**
   * Löscht den Mitglied und aktualisiert die Anzeige
   * @param nrs Customers Nr
   * @return Boolean
   */
  def deleteCustomer(nrs: Int*) = Action {
    val deleted = nrs.count(nr => {
      if (Mitglied.delete(nr) > 0) {
        Logger.add("Mitglied Nr. %d gelöscht" format nr)
        true
      } else {
        Logger.add("Mitglied mit der Nr. %d wurde nicht gefunden" format nr)
        false
      }
    })
    Redirect(routes.MitgliederController.getMitgliederListe(0)).flashing("info" -> ("%d gelöscht " format deleted))
  }

  /**
   * Fügt einen neuen Kontakteintrag zum Mitglied hinzu, wenn dieser existiert und fordert Aktualisierung der Anzeige an
   *
   * @param mitglied Mitglied
   * @param k Kontakt
   * @return Boolean
   */
  def addContact(mitglied: Mitglied, k:Kontakt) = {
    if (exists(mitglied.nr)) {
      mitglied.kontaktList +:= k

      true
    } else {
      false
    }
  }

  /**
   * Löscht den angegebenen Kontakt aus der Kontaktliste des Mitglieds
   * @param cnr Mitgliedernr.
   * @param k Kontakt
   * @return Boolean
   */
  def delContact(cnr: Int, k:Kontakt) = {
    if (customer.list.isDefinedAt(cnr) && customer(cnr).kontaktList.exists(_ eq k)) {
      customer.list(cnr).kontaktList = customer.list(cnr).kontaktList.filterNot(_ eq k)
      true
    } else {
      false
    }
  }

  /**
   * Anzahl der Mitglieder im Model
   * @return Int
   */
  def custCount = customer.list.size

  /**
   * Prüft, ob ein Mitglied mit der Nr. existiert
   * @param nr Mitgliedernr.
   * @return Boolean
   */
  def exists(nr: Int): Boolean = customer.list.isDefinedAt(nr)

  def mitgliedDetails(nr: Int) = Action {
    customer.list.get(nr).map { m =>
      Ok(views.html.mitglieder_details(m, arbeitsstdForm))
    }.getOrElse(NotFound("nichts gefunden"))
  }

  def importMitglieder = Action {
    val cust: Promise[List[Mitglied]] = Akka.future {
      (1 to 10).foldLeft(List[Mitglied]())((l, i) => l ::: new CsvReader(new java.io.File("public/mitglieder.csv")).parseCustomers)
    }

    Async {
      cust.map { c =>
        if (c.size > 0) {
          Cache.set("imp", "fertig")
          Redirect(routes.MitgliederController.getMitgliederListe(0)).flashing("info" -> ("%d Mitglieder importiert" format c.size))
        } else
          BadRequest
      }
    }

  }

  def saveMitglieder = Action {
    //Save to File

    Redirect(routes.MitgliederController.getMitgliederListe(0)).flashing (
      if (MainController.saveAll())
        ("info" -> "erfolgreich gespeichert")
      else
        ("error" -> "Fehler beim Speichern")
    )
  }

  def processDelArbStd(mnr: Int, stdNr: Int) = Action {
    customer.list.get(mnr) match {
      case Some(m) if m.arbeitsStunden.size >= stdNr =>
        deleteArbeitsstunde(m, m.arbeitsStunden(stdNr));
        Redirect(routes.MitgliederController.getMitgliederListe(m.nr)).flashing("info" -> "Arbeitsstunde gelöscht")
      case _ => Redirect(routes.MitgliederController.getMitgliederListe(0)).flashing("error" -> "Mitglied oder Arbeitsstunde nicht gefunden")
    }
  }

  def deleteArbeitsstunde(m: Mitglied, std: Arbeitsstunde) = {
    m.arbeitsStunden = m.arbeitsStunden.filterNot(std eq)
  }

  def processNewArbeitsstunden(nr: Int) = Action { implicit request =>
    customer.list.get(nr).map { m =>
      arbeitsstdForm.bindFromRequest.fold(
        formWithErrors => {
          BadRequest(views.html.mitglieder(getCustList, Some(m), formWithErrors)).flashing("error" -> "Es ist ein Fehler aufgetreten")
        },{
          arb: Arbeitsstunde =>
            m.arbeitsStunden = arb :: m.arbeitsStunden
            Redirect(routes.MitgliederController.getMitgliederListe(m.nr)).flashing("info" -> "neue Arbeitsstunde hinzugefügt")
        }
      )
    }.getOrElse(BadRequest(views.html.mitglieder(customer.list.values.toList)))
  }

}


