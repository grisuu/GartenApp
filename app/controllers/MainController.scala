package controllers

import java.io.File
import models._
import tools.fileio._

object MainController {

  val appName   = "GartenApp2"
  val version   = "2.0"
  val autor     = "Mario Seidel"
  val autorMail = "mario.seidel@web.de"
  val debugMode = true

  /**
   * os-based file seperator
   */
  val /\ = sys.props.get("file.separator").getOrElse("/")

  val savePath = new File("data")
  val mainFile = new File(savePath.getAbsolutePath + /\ + "main.dat")
  val notesFile = new File(savePath.getAbsolutePath + /\ + "notes.dat")
  val customersFile = new File(savePath.getAbsolutePath + /\ + "cust.dat")
  val gartenFile = new File(savePath.getAbsolutePath + /\ + "garten.dat")
  val vertragFile = new File(savePath.getAbsolutePath + /\ +"vertrag.dat")
  val imagePath = new File(savePath.getAbsolutePath + /\ +  "images")

  lazy val mainModel: MainModel = new ObjectSaver().loadFromFile[MainModel](mainFile)
/*  val custCont  = MitgliederController
  val gartCont  = GartenController
  val pachtCont = PachtvertragController*/

  def getImgFile(name: String) = {
    val img = new File(imagePath.getAbsolutePath + /\ + name)
    if (img.isFile) img else new File(imagePath.getAbsolutePath + /\ + "no_image.png")
  }

  /**
   * öffnet Explorer (nur Win) mit dem aktuellen Arbeitsverzeichnis
   */
  def openWorkDir() {
    scala.sys.process.stringToProcess("explorer.exe " + new File(".").getAbsolutePath).!
  }

  def saveAll() = {
    new ObjectSaver().saveToFile[MainModel](mainFile, mainModel)
  }

	def startup() {
    //if (!savePath.isDirectory) savePath.mkdir()


    scala.sys.props.foreach(f => Logger.add("%s = %s" format (f._1, f._2)))
    Logger.add(List.fill(50)("=").mkString("~"))
    Logger.add("%s Version: %s" format (appName, version))
    Logger.add(List.fill(50)("=").mkString("~"))
	}

}