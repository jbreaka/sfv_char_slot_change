package jbreaka.io

import java.io.File

import jbreaka.capcom.CharacterCodes
import jbreaka.capcom.CharacterCodes.{Pak}

class FileManager {
  def manage(pak:Pak, newSlot:Short, files:Set[File])={
    renameIndividualFiles(pak, newSlot, files)
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

  def renameIndividualFiles(pak:Pak, newSlot:Short, files:Set[File])={
    val currentCharCode = CharacterCodes.getSfvCharCode(pak)
    val keyFilenames = Set[String]("Costume","Preview","Setting","Preset","Material","Prop")
    /*  Do Change these
    Costume_02 to Costume_01
    Preview_02 to Preview_01
    Setting_02 to Setting_01
    Preset_02 to Preset_01
    Material_02 to Material_01 (for Akuma, Ed and Ibuki)
    Prop_02 to Prop_01 (for Falke, Menat and Vega)
    */


    val strCurrSlot = int2Str(pak.slot)
    val strSlot = int2Str(newSlot)
    val newNames = for {
      file <- files
      prevName = file.getName
      keyFilename <- keyFilenames
      if(prevName.contains(keyFilename))
      newName = prevName.
        replace(s"${keyFilename}_${strCurrSlot}", s"${keyFilename}_${strSlot}").
        replace(s"${currentCharCode}_${strCurrSlot}", s"${currentCharCode}_${strSlot}")
    } yield newName
    newNames.foreach(println)
    null
  }
}
