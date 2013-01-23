package models

import org.joda.time.{DateTime, DateMidnight}
import controllers.PachtvertragController
import play.db.ebean.Model

object MitgliedsTyp extends Enumeration with Serializable {
  val VORSITZENDER  = Value(1, "Vorstandsvorsitzender")
  val VORSTAND      = Value(2, "Vorstand")
  val MITGLIED      = Value(3, "Mitglied")
}

case class Mitglied(nr: Int, var anrede: Sex.Value,
                             var vorname: String,
                             var nachname: String,
                             var adresse: Adresse,
                             createDate: DateTime = new DateTime(),
                             var dob: DateMidnight = new DateMidnight(),
                             var position: MitgliedsTyp.Value = MitgliedsTyp.MITGLIED) extends Model {

  var kontaktList: List[Kontakt] = List()

  var arbeitsStunden: List[Arbeitsstunde] = List()

/*  def pachtverträge: List[Pachtvertrag] = {
    val pacht = new PachtvertragController
    pacht.getVerträge(this)
  }*/

  def formAnrede = "%s %s %s" format (anrede, vorname, nachname)
  def briefAnrede = if (anrede == Sex.FEMALE) "Sehr geehrte Gartenfreundin " + formAnrede else "Sehr geehrter Gartenfreund " + formAnrede
  def nachVorname = "%s, %s" format (nachname, vorname)

  def garten: List[Garten] = PachtvertragController.pachtverträge.list.values.filter(_.mitglied eq this).map(_.garten).toList

  override def toString = "Mitglied Nr. %3d - %s" format (nr, formAnrede)
}

object Mitglied {
  import anorm._
  import anorm.SqlParser._
  import play.api.db._
  import play.api.Play.current

  val mitglied = {
    int("id") ~
    int("anrede") ~
    str("vorname") ~
    str("nachname") ~
    str("strasse") ~
    str("hausnr") ~
    str("plz") ~
    str("ort") ~
    str("land") ~
    date("create_date") ~
    date("geburtstag") ~
    int("position") map {
      case id~anrede~vorname~nachname~strasse~hausnr~plz~ort~land~create_date~geburtstag~position =>
        Mitglied(id, Sex(anrede), vorname, nachname, Adresse(strasse, hausnr, plz, ort, land), new DateTime(create_date), new DateMidnight(geburtstag), MitgliedsTyp(position))
    }
  }

  def all(): List[Mitglied] = DB.withConnection { implicit c =>
    SQL("select * from mitglieder").as(mitglied *)
  }

  def find(id: Int) = DB.withConnection { implicit c =>
    SQL("select * from mitglieder where id = {id}").on(
      'id -> id
    ).as(mitglied singleOpt)
  }

  def create(m: Mitglied) {
    DB.withConnection { implicit c =>
      SQL("""insert into mitglieder (anrede, vorname, nachname, strasse, hausnr, plz, ort, land, create_date, geburtstag, position) values
            ({anrede}, {vorname}, {nachname}, {strasse}, {hausnr}, {plz}, {ort}, {land}, {create_date}, {geburtstag}, {position})
          """).on(
        'anrede       -> m.anrede.id,
        'vorname      -> m.vorname,
        'nachname     -> m.nachname,
        'strasse      -> m.adresse.str,
        'hausnr       -> m.adresse.hausnr,
        'plz          -> m.adresse.plz,
        'ort          -> m.adresse.ort,
        'land         -> m.adresse.land,
        'create_date  -> m.createDate.toDate,
        'geburtstag   -> m.dob.toDate,
        'position     -> m.position.id
      ).executeUpdate()
    }
  }

  def update(m: Mitglied) {
    DB.withConnection { implicit c =>
      SQL("""UPDATE mitglieder set anrede = {anrede}, vorname = {vorname}, nachname = {nachname}, strasse = {strasse},
             hausnr = {hausnr}, plz = {plz}, ort = {ort}, land = {land}, create_date = {create_date}, geburtstag = {geburtstag}, position = {position}
             WHERE id = {id}
          """).on(
        'anrede       -> m.anrede.id,
        'vorname      -> m.vorname,
        'nachname     -> m.nachname,
        'strasse      -> m.adresse.str,
        'hausnr       -> m.adresse.hausnr,
        'plz          -> m.adresse.plz,
        'ort          -> m.adresse.ort,
        'land         -> m.adresse.land,
        'create_date  -> m.createDate.toDate,
        'geburtstag   -> m.dob.toDate,
        'position     -> m.position.id,
        'id           -> m.nr
      ).executeUpdate()
    }
  }

  def delete(id: Int) = {
    DB.withConnection { implicit c =>
      SQL("delete from mitglieder where id = {id} limit 1").on(
        'id -> id
      ).executeUpdate()
    }
  }
}