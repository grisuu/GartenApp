package models

import tools.helper._
import java.util.{Calendar, Date}
import collection.immutable.TreeMap

/**
 * Created with IntelliJ IDEA.
 * User: PathTracer
 * Date: 08.12.12
 * Time: 14:21
 */

object Logga {
  var output :(String) => Unit = println
  var logs: TreeMap[Date, String] =  new TreeMap()(Ordering[Date].reverse)

  def add(text: String) {
    logs = logs ++ TreeMap[Date, String](Calendar.getInstance.getTime -> text.toString)
    output(lastLog)
  }

  def add(obj: {def toString(): String}) {
    add(obj.toString)
  }

  def lastLog = "%s - %s".format(Formatta.dateToStr(logs.head._1), logs.head._2)
}
