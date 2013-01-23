package tools.helper

import play.api.data.format.{Formats, Formatter}
import java.util.{Locale, Date}
import java.text.{NumberFormat, ParseException, SimpleDateFormat}
import org.joda.time.DateTime
import models.Logger
import play.api.data.FormError

/**
 * gartenapp2.apps.common 2012
 *
 * User: PathTracer
 * Date: 17.12.12
 * Time: 10:39
 */
object Formatta {

  /**
   * Formatiert ein Datum zu einem String
   * @param date Date
   * @param pattern String (z.B. "dd.MM.yyyy HH:mm:ss")
   * @return String
   */
  def dateToStr(date :Date,pattern: String = "dd.MM.yyyy HH:mm:ss") = new SimpleDateFormat(pattern).format(date)

  def strToDate(str: String): Option[DateTime] = {

    val reg = """\d{2}.\d{2}.\d{4}""".r
    val reg1 = """\d{2}.\d{2}.\d{2}""".r
    val dp = new SimpleDateFormat("dd.MM.yyyy")

    if ((reg findFirstIn str.trim).isDefined) {
      dp.applyPattern("dd.MM.yyyy")
    } else if ((reg1 findFirstIn str.trim).isDefined) {
      dp.applyPattern("dd.MM.yy")
    } else {
      Logger.add("falsches Datumsformat")
      return None
    }

    try {
      Some(new DateTime(dp.parse(str.trim)))
    } catch {
      case (_ :ParseException | _ :IllegalArgumentException) => Logger.add("Datum konnte nicht eingelesen werden: " + str); None
    }
  }

  def round(zahl: Double, decimalPlaces :Int = 2):Double = BigDecimal(zahl).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble

  implicit def doubleFormat = new Formatter[Double] {
    def bind(key: String, data: Map[String, String]) = {
      Formats.stringFormat.bind( key, data ).right.flatMap { s =>
        scala.util.control.Exception.allCatch[Double]
          .either{
            val df = NumberFormat.getInstance(Locale.GERMAN)
            val res = df.parse(s)
            res.doubleValue()
          }
          .left.map( e => Seq( FormError( key, "error.double", Nil ) ) )
      }

    }
    def unbind(key: String, value: Double) = Map(key -> value.toString)
  }

  private def deepCopy[A](a: A)(implicit m: reflect.Manifest[A]): A =
    util.Marshal.load[A](util.Marshal.dump(a))
}
