package tools.fileio

import java.io.File
import java.util.Locale
import java.text.{DecimalFormat, ParseException, SimpleDateFormat}
import org.joda.time.{DateTime, DateMidnight}
import models._
import controllers.{GartenController, MitgliederController, PachtvertragController, MainController}
import io.Codec

/**
 * Created with IntelliJ IDEA.
 * User: PathTracer
 * Date: 07.12.12
 * Time: 23:49
 */
class CsvReader(pfad: File) {

  lazy val lines = scala.io.Source.fromFile(pfad, Codec.UTF8.name()).getLines().toList
  /**
    * Parst Mitglieder aus einer CSV heraus
   * @return List[Customers]
   */
  def parseCustomers: List[Mitglied] = {
    val custCont  = MitgliederController
    val gartCont  = GartenController
    /*val pachtCont = MainController.pachtCont*/

    lines.foldLeft(List[Mitglied]()) { (l: List[Mitglied], line) =>
      line.split('|').toList match {
        case List(nr:String, nn:String, vn:String, plz:String,ort:String,str,hnr,tel:String,email:String,dob:String,pacht,laube,grundb) if nn.nonEmpty =>

          val dp = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)

          val gebDate = try {
            new DateMidnight(dp.parse(dob))
          } catch {
            case (_ :ParseException | _ :IllegalArgumentException) => println("Datum konnte nicht eingelesen werden: " + dob); new DateMidnight()
          }

          val c = custCont.addMitglied(Sex.MALE, vn, nn, str, hnr, plz, ort, gebDate)
          if (email.nonEmpty) c.kontaktList +:= Kontakt(vn + " " + nn, email, KontaktType.Email)
          if (tel.nonEmpty && tel.substring(0,2) == "01") c.kontaktList +:= Kontakt(vn + " " + nn, tel, KontaktType.Mobile)
          if (tel.nonEmpty && tel.substring(0,4) == "0371") c.kontaktList +:= Kontakt(vn + " " + nn, tel, KontaktType.Tel)
          if (tel.nonEmpty && tel.substring(0,1) != "0") c.kontaktList +:= Kontakt(vn + " " + nn, "0371 " + tel, KontaktType.Tel)

          try {
            val gart = gartCont.addGarten(nr.toInt, pacht.replace(',','.').toDouble, laube.replace(',','.').toDouble, grundb.replace(',','.').toDouble)
            if (gart.isDefined)
              PachtvertragController.addPachtvertrag(c, gart.get, new DateTime())
          } catch {
            case _: NumberFormatException => println("could not parse line")
            case e: Exception => println(e)
          }

          c :: l
        case _ => l
      }
    }
  }

  def parseVerbrauch(datum: DateTime) = {
    val df = new DecimalFormat("#,##")
    lines.foldLeft(Map[Int, (Double, Double)]()) { (l: Map[Int, (Double, Double)], line) =>
      line.split('|').toList match {
        case List(nr:String, nn:String, _, _, _, _, pacht: String, stromAlt: String, stromNeu: String, _, _, wasserAlt: String, wasserNeu: String, _, _, gartenBeitrag: String, strWasBetr: String, _, mitgliedBeitrag: String, grundB: String, _) if nr.nonEmpty =>
          try {
            Map(nr.toInt -> (df.parse(stromNeu).doubleValue(), df.parse(wasserNeu).doubleValue())) ++ l
          } catch {
            case e: Exception => println(e); l
          }
        case _ => l
      }
    }
  }
}
