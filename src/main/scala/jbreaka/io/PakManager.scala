package jbreaka.io

import java.io.{BufferedOutputStream, BufferedReader, File, FileOutputStream, InputStreamReader, PrintWriter}
import java.nio.charset.StandardCharsets

import scalaz.\/

import scala.io.Source
import scala.jdk.CollectionConverters._
import scalaz._
import Scalaz._

object PakManager {

  /**
   * Extract the file and return the extracted Files.
   */
  def read(u4pak:File, file:File):Set[File]= {
    val files = executeU4pakList(u4pak, file)
    files.foreach(f => {
      println(s"$f exists? ${f.exists()}")
    })
    executeU4pakUnpack(u4pak, file)
    files
  }

  def write2Disk(destination:File,contents:Array[Byte]):Exception\/File = \/.fromTryCatchNonFatal{
    println("Writing the new PAK contents to disk")
    val bos = new BufferedOutputStream(new FileOutputStream(destination))
    bos.write(contents)
    bos.close() // You may end up with 0 bytes file if not calling close.
    destination
  }.leftMap(_.asInstanceOf[Exception])

  def write2Disk(destination:File,contents:String):Exception\/File = \/.fromTryCatchNonFatal{
    val writer = new PrintWriter(destination)
    println("Writing the new PAK contents to disk")
    writer.write(contents)
    writer.close()
    destination
  }.leftMap(_.asInstanceOf[Exception])

  def writeU4pakToDisk()={
    val script = File.createTempFile("u4pak","py")
    script.deleteOnExit()
    script.setExecutable(true)
    val u4pakStr = Source.fromFile("src/main/resources/u4pak.py").mkString
    write2Disk(script,u4pakStr) match {
      case -\/(e) => e.printStackTrace()
        null
      case \/-(f) => f
    }
  }

  private def executeU4pak(u4pak:File, pak2conv:File, command:String)={
    val params = List[String]("python", u4pak.getCanonicalPath,command,pak2conv.getCanonicalPath).asJava
    val process = new ProcessBuilder(params).start()

    process.getInputStream()
  }


  private def executeU4pakUnpack(u4pak:File, pak2conv:File)={
    val stdout = executeU4pak(u4pak,pak2conv,"unpack")
    println(Source.fromInputStream(stdout,StandardCharsets.UTF_8.name()).mkString)
  }

  private def executeU4pakList(u4pak:File, pak2conv:File)={
    val stdout = executeU4pak(u4pak,pak2conv,"list")
    val files = for{
      line <- Source.fromInputStream(stdout,StandardCharsets.UTF_8.name()).getLines().toSet[String]
      file = new File(line)
    } yield file
    files
  }

  def executeU4pakPack(u4pak:File, pak2conv:File)={
    val params = List[String]("python", u4pak.getCanonicalPath,"pack",pak2conv.getCanonicalPath,"StreetFighterV").asJava
    val process = new ProcessBuilder(params).start()

    val stdout = process.getInputStream()
    val files = for{
      line <- Source.fromInputStream(stdout,StandardCharsets.UTF_8.name()).getLines().toSet[String]
      file = new File(line)
    } yield file
    files
  }
}
