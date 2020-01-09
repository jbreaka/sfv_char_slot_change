package jbreaka

import java.io.File
import jbreaka.capcom.CharacterCodes
import jbreaka.io.{FileManager, PakManager}

object Main extends App {
  val pm = new PakManager()
  val fm = new FileManager()

  val pak2Conv = new File("src/test/resources/paks/PoisonC2-Catwoman.pak")
  val u4pak = pm.writeU4pakToDisk()

  val extractedFiles = pm.read(u4pak,pak2Conv)
  println("Extract Files")
  println("==============================")
  extractedFiles.foreach(println)
  println("==============================")


  CharacterCodes.analyzePakPath(extractedFiles.head.getCanonicalPath).
    foreach(pak => {
      println(s"Found PAK details $pak")
      fm.manage(pak,3,extractedFiles)
    })
  val archive:File = new File("PoisonC3.pak")
  pm.executeU4pakPack(u4pak, archive)

  println("Done.")
}
