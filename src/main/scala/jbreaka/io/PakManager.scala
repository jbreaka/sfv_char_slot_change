package jbreaka.io

import java.io.{BufferedReader, File, InputStreamReader, PrintWriter}
import java.nio.charset.StandardCharsets

import scala.io.Source
import scala.jdk.CollectionConverters._

class PakManager {

  /**
   * Extract the file and return the extracted Files.
   */
  def read(file:File):Set[File]= {
    val u4pak = writeU4pakToDisk()
//    println(Source.fromFile(u4pak).mkString)
    val files = executeU4pakList(u4pak, file)
    files.foreach(f => {
      println(s"$f exists? ${f.exists()}")
    })
    executeU4pakUnpack(u4pak, file)
    files
  }

  private def writeU4pakToDisk()={
    val script = File.createTempFile("u4pak","py")
    script.deleteOnExit()
    script.setExecutable(true)

    val u4pakStr = Source.fromFile("src/main/resources/u4pak.py").mkString
    val writer = new PrintWriter(script)
    writer.write(u4pakStr)
    writer.close()

    script
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

}
