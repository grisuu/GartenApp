package controllers

import org.joda.time.DateTime
import models._
import play.api.mvc._
import views.html
import play.api.data.Form
import play.api.data.Forms._
import models.Mitglied
import models.Garten
import models.Pachtvertrag

object PachtvertragController extends Controller {


  val pachtverträge: PachtvertragModel = MainController.mainModel.pachtModel

  val pachtForm: Form[Pachtvertrag] = Form(
    mapping(
      "nr"          -> ignored(getNewPachtNr),
      "begin"       -> date("yyyy-MM-dd").transform(new DateTime(_), (d: DateTime) => d.toDate),
      "pächter"     -> nonEmptyText.transform[Mitglied](s => MitgliederController.customer.apply(s.toInt), _.nachVorname),
      "garten"      -> nonEmptyText.transform[Garten](s => GartenController.gärten(s.toInt), _.toString)
    )(Pachtvertrag.apply)(Pachtvertrag.unapply)
  )

  def getNewPachtNr: Int = { pachtverträge.lastPachtNr += 1; pachtverträge.lastPachtNr }

  def getPachtvertragList = Action { implicit request =>
    Ok(html.pachtvertraege(pachtverträge.values.toList.sortBy(_.nr) ))
  }

  def addPachtvertrag(p: Pachtvertrag): Boolean = {
    val newNr = getNewPachtNr
    val newPacht = p.copy(newNr)
    pachtverträge.list += newNr -> newPacht

    true
  }

  def addPachtvertrag(mitglied: Mitglied, garten: Garten, begin: DateTime): Boolean = {
    addPachtvertrag(new Pachtvertrag(getNewPachtNr, begin, mitglied, garten))
  }

  def processNewPachtvertrag = TODO

  def newPachtvertrag = TODO

  def pachtvertragDetail(nr: Int) = TODO

  def addMitglied = TODO /*implicit request =>
    pachtForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(html.mitglieder_form(formWithErrors)) },
      { case m: Mitglied =>
          addPachtvertrag(m)
          Redirect(routes.MitgliederController.getMitgliederListe()).flashing("info" -> "Mitglied hinzugefügt")
        case e => println(e); BadRequest("geht ne")
      }

    )*/


  def showPachtvertrag(mId: Int) = TODO
  /*Action {
    customer.list.get(mId).map { m =>
      Ok(views.html.mitglieder_form(pachtForm.fill(m)))
    }.getOrElse(NotFound("nicht gefunden"))
  }*/


}


