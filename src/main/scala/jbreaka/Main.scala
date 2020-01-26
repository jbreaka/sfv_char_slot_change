package jbreaka

import java.io.File

import jbreaka.capcom.CharacterCodes
import jbreaka.io.{FileManager, PakManager, SfvRegEx}
import scalaz._
import Scalaz._
import scala.jdk.CollectionConverters._

object Main extends App {
  val javaVersion = System.getProperty("java.version").split("\\.")(0).toInt
  println(s"Java Version= $javaVersion")
  if(javaVersion < 12){
    println(s"You are using Java version $javaVersion, but this application requires you to have Java Version 12 or greater. Please download the latest JRE or JDK from Oracle.\n"+
    "\nhttps://www.oracle.com/technetwork/java/javase/downloads/index.html")
    System.exit(0)
  }

val usage = """sfv_char_slot_change <new slot number> <original PAK> <new PAK>
ie.  sfv_char_slot_change 2 poisonC1-Catwoman.pak poisonC2-Catwoman.pak


"""
  require(args.length > 2,usage)
  val newSlot = args(0).toShort

  def verifyFileArg(file:File): Unit ={
    require(pak2Conv.exists(),s"The file $file does not exist.")
    require(pak2Conv.isFile,s"The $file provided is not actually a file. This argument needs to be a file.")
    require(pak2Conv.canRead,s"Cannot read the file $file.")
  }
  val pak2Conv = new File(args(1))
  verifyFileArg(pak2Conv)
  val destination = new File(args(2))
  verifyFileArg(destination)

  for{
    contentStr <- FileManager.fileToString(pak2Conv)
    pak <- CharacterCodes.analyzeContent(contentStr) \/> new Exception("Could not determine details about the PAK")
    _ = println(s"Found $pak")
    newStr = SfvRegEx.replaceStrings(pak.character, newSlot, pak.slot, contentStr)
  } {
    println("the new string was created.")
    PakManager.write2Disk(destination,newStr)

  }.swap.foreach(_.printStackTrace())
  println("Done.")
}
