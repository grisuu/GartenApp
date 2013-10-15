package models

import util.Random
import org.joda.time.{DateMidnight, DateTime}
import scala.slick.driver.H2Driver.simple._
import Database.threadLocalSession
import play.api.db.DB

/**
 * gartenapp2.apps.common 2012
 *
 * User: PathTracer
 * Date: 17.12.12
 * Time: 00:05
 */
case class Garten(nr: Int, fläche: Float, laube: Float, grundB: Float, stromNr: Int = 0, wasserNr: Int = 0) {

  val wasseruhr   = Wasserzaehlers.find(wasserNr)
  val stromzähler = Stromzaehlers.find(stromNr)

  override def toString = "Garten Nr. %3d" format nr
}


object Garten extends Table[Garten]("gaerten") {

  def id          = column[Int]("id", O.PrimaryKey)
  def fläche      = column[Float]("flaeche")
  def laube       = column[Float]("laube")
  def grundB      = column[Float]("grund_b")
  def stromNr     = column[Int]("strom_nr")
  def wasserNr    = column[Int]("wasser_nr")
  def * = id ~ fläche ~ laube ~ grundB ~ stromNr ~ wasserNr <> (Garten, Garten.unapply _)

  def all(): List[Garten] = Database.forDataSource(DB.getDataSource()).withSession {
    (for (m <- Garten) yield (m)).list
  }

  def find(id: Int) = Database.forDataSource(DB.getDataSource()).withSession {
    Garten.filter(_.id === id)
  }

  def create(g: Garten) = Database.forDataSource(DB.getDataSource()).withSession {
    Garten insert g
  }

  def update(g: Garten) = Database.forDataSource(DB.getDataSource()).withSession {
    Garten.filter(_.id === g.nr).update(g)
  }

  def delete(id: Int) = Database.forDataSource(DB.getDataSource()).withSession {
    Garten.filter(_.id === id).mutate(_.delete())
  }
}