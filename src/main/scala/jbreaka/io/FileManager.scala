package jbreaka.io

import java.io.File

import jbreaka.capcom.CharacterCodes
import jbreaka.capcom.CharacterCodes.Pak

import scala.annotation.tailrec

class FileManager {
  def manage(pak:Pak, newSlot:Short, files:Set[File])={
    val currSlotStr = int2Str(pak.slot)
    val newSlotStr = int2Str(newSlot)
    val oldCharCode = pak.code
    val newCharCode = CharacterCodes.getSfvCharCode(pak.character,newSlot).get
    val DIFF_CHAR_CODE = oldCharCode != newCharCode
    println(s"oldCharCode($oldCharCode) == newCharCode($newCharCode)  $DIFF_CHAR_CODE")

    val keyFilenames = Set[String]("Costume","Preview","Setting","Preset","Material","Prop")
    val oldKeyFilenames = keyFilenames.map(_ + s"_${currSlotStr}")
    val newKeyFilenames = keyFilenames.map(_ + s"_${newSlotStr}")
    val keyFileNamesPairing:Set[(String,String)] = oldKeyFilenames.zip(newKeyFilenames)
    /**
     * only called with files that need to be renamed
     */
    def renameFile(file:File)={
      val fixedName = keyFileNamesPairing.foldLeft(file.getName())((name,pair) => {name.replace(pair._1,pair._2)})

      val newName = fixedName.
        replace(s"${oldCharCode}_${currSlotStr}", s"${newCharCode}_${newSlotStr}").
        replace(s"_${oldCharCode}_", s"_${newCharCode}_")
      val finalNewName =  if(newName.startsWith(s"${oldCharCode}_")) newName.replace(s"${oldCharCode}_",s"${newCharCode}_")
                          else newName
      val newCanonicalPath = file.getParentFile.getPath + File.separator + finalNewName
      val newFile = new File(newCanonicalPath)
      val renamed = file.renameTo(newFile)
      if(!renamed) println(s"Failed to rename $file.getName to $finalNewName")
      newFile
    }

    println(s"See if ${files.size} files need to be renamed")


    val renamedFiles = for {
      file <- files
    } yield renameFile(file)
    println(s"Renamed ${renamedFiles.size} individual files")
    renamedFiles.foreach(println)

    @tailrec
    def folderConversions(rfiles:Set[File]):Unit = {
      if(!rfiles.isEmpty){
        for{
          file <- rfiles
          name = file.getName
        } yield {
          println(s"checking for folder conversion...$name")
          if(name == currSlotStr){
            val newCanonicalPath = file.getParentFile.getPath + File.separator + newSlotStr
            val renamed = file.renameTo(new File(newCanonicalPath))
            println("::slot::"+renamed)
          } else if(DIFF_CHAR_CODE && name == oldCharCode) {
            val newCanonicalPath = file.getParentFile.getPath + File.separator + newCharCode
            val renamed = file.renameTo(new File(newCanonicalPath))
            println("--code--"+renamed)
          }
        }

        folderConversions(rfiles.map(_.getParentFile).filter(_.getName != "StreetFighterV"))
      }
    }

    folderConversions(renamedFiles.map(_.getParentFile))
    println("Finished folder conversions")
  }

  private def int2Str(num:Int) = if( num < 10) s"0$num" else num.toString


}
