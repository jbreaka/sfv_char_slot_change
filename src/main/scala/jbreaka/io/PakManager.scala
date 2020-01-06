package jbreaka.io

import java.io.{BufferedReader, File, InputStreamReader, PrintWriter}
import java.nio.charset.StandardCharsets

import scala.io.Source
import scala.jdk.CollectionConverters._

class PakManager {
  val charCodes = characterCodes()

  def read(file:File)= {
    val u4pak = writeU4pakToDisk()
//    println(Source.fromFile(u4pak).mkString)
    executeU4pakList(u4pak, file).foreach(f => {
      println(s"$f exists? ${f.exists()}")
    })
    executeU4pakUnpack(u4pak, file)
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

  private def int2Str(num:Int) = if( num < 10) s"0$num" else num.toString

  protected def internalFolderConversion(currentSlot:Int,newSlot:Int, file:String)={
    val currStr = s"/${int2Str(currentSlot)}/"
    val newStr = s"/${int2Str(newSlot)}/"
    file.replaceAll(currStr,newStr)
  }

  protected def charcterSlotConversion(currentSlot:Int,newSlot:Int, file:String, charCode:String)={
    val currStr = s"${charCode}_${int2Str(currentSlot)}"
    val newStr = s"${charCode}_${int2Str(newSlot)}"
    file.replaceAll(currStr,newStr)
  }

  protected def complexConversion(currentSlot:Int,newSlot:Int, file:String)={
    val currStr = s"_${int2Str(currentSlot)}"
    val newStr = s"_${int2Str(newSlot)}"
    /*  Do Change these
    Costume_02 to Costume_01
    Preview_02 to Preview_01
    Setting_02 to Setting_01
    Preset_02 to Preset_01
    Material_02 to Material_01 (for Akuma, Ed and Ibuki)
    Prop_02 to Prop_01 (for Falke, Menat and Vega)
    */

    //TODO: Avoid bones, textures, and physics assets
    /*COLOR_02
    NORMAL_02
    MASK_02
    SRMA_02
    SSS_02

    THESE SHOULD NOT BE REPLACED, if you replace COLOR_02 into COLOR_01 then you will screw up your textures. Similarly entries like

    HAIR_02
    RIBBON_02
    LACE_02
    POUCH_02*/
    file.replaceAll(currStr,newStr)
  }

  private def characterCodes():Set[String]=for {
    line <- Source.fromFile("src/main/resources/character_codes.csv").getLines().toSet[String]
    code = line.split(",")(0)
  } yield code
}
