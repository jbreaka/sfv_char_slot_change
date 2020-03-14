package jbreaka.io

import java.io.{File, FileInputStream, FileOutputStream, IOException, PrintWriter}

import jbreaka.capcom.CharacterCodes
import jbreaka.capcom.CharacterCodes.Pak
import scalaz._
import Scalaz._
import scala.annotation.tailrec
import scala.io.Source

object FileManager {

  private def int2Str(num:Int) = if( num < 10) s"0$num" else num.toString

  /**
   * Read binary file from disk and translate it to string
   */
  def fileToBytes(file:File):Exception\/Array[Byte] = \/.fromTryCatchNonFatal{
    val in = new FileInputStream(file)
    in.readAllBytes()
  }.leftMap(t=> if(t.isInstanceOf[Exception]) t.asInstanceOf[Exception] else new Exception(t))

  def bytesToString(bytes:Array[Byte]):String = (bytes.map(_.toChar)).mkString

  def bytesToStringWithCheck(bytes:Array[Byte],file:File):Exception\/String = {
    val str = bytesToString(bytes)
    if(bytes.length != file.length()) new IllegalStateException(s"Only was able to read ${bytes.length} but should have read ${file.length()}").left[String]
    else if(str.length != file.length()) new IllegalStateException(s"Converting bytes to string caused loss of data. String is ${str.length} but should have been ${file.length()}").left[String]
    else str.right[Exception]
  }

  def fileToString(file:File):Exception\/String = {
    fileToBytes(file).map(by => {
      bytesToString(by)
    }) match {
      case \/-(bys) if(bys.length != file.length())=> new IllegalStateException(s"Only was able to read ${bys.length} but should have read ${file.length()}").left[String]
      case v => v
    }
  }

  def manage(pak:Pak, newSlot:Short, files:Set[File])={

    //Slot for costume
    val CURR_SLOT_STR = int2Str(pak.slot)
    val NEW_SLOT_STR = int2Str(newSlot)

    //Character codes
    val OLD_CHAR_CODE = pak.code
    val NEW_CHAR_CODE = CharacterCodes.getSfvCharCode(pak.character,newSlot).get
    val DIFF_CHAR_CODE = OLD_CHAR_CODE != NEW_CHAR_CODE
    println(s"oldCharCode($OLD_CHAR_CODE) == newCharCode($NEW_CHAR_CODE)  $DIFF_CHAR_CODE")

    val OLD_CODE_AND_SLOT = s"${OLD_CHAR_CODE}_${CURR_SLOT_STR}"
    val NEW_CODE_AND_SLOT = s"${NEW_CHAR_CODE}_${NEW_SLOT_STR}"

    val OLD_CODE_BARS = s"_${OLD_CHAR_CODE}_"
    val NEW_CODE_BARS = s"_${NEW_CHAR_CODE}_"

    //Pairing key files and slots
    val keyFilenames = List[String]("Costume","Preview","Setting","Preset","Material","Prop")
    val oldKeyFilenames = keyFilenames.map(_ + s"_${CURR_SLOT_STR}")
    val newKeyFilenames = keyFilenames.map(_ + s"_${NEW_SLOT_STR}")
    val keyFileNamesPairing:Set[(String,String)] = oldKeyFilenames.zip(newKeyFilenames).toSet

    def stringAdjustment(str:String):String = {
      val fixedName = keyFileNamesPairing.foldLeft(str)((name,pair) => {name.replaceAll(pair._1,pair._2)})
      val newStr = fixedName.
        replaceAll(OLD_CODE_AND_SLOT,NEW_CODE_AND_SLOT).
        replaceAll(OLD_CODE_BARS, NEW_CODE_BARS).
        replaceAll(s"/$CURR_SLOT_STR/",s"/$NEW_SLOT_STR/").
        replaceAll(s"/$OLD_CHAR_CODE",s"/$NEW_CHAR_CODE")
      newStr
    }

    /**
     * only called with files that need to be renamed
     */
    def renameFile(file:File)={
      editFile(file)

      val newName = stringAdjustment(file.getName)
      val finalNewName =  if(newName.startsWith(s"${OLD_CHAR_CODE}_")) newName.replace(s"${OLD_CHAR_CODE}_",s"${NEW_CHAR_CODE}_")
                          else newName
      val newCanonicalPath = file.getParentFile.getPath + File.separator + finalNewName
      val newFile = new File(newCanonicalPath)
      val renamed = file.renameTo(newFile)
      if(!renamed) println(s"Failed to rename $file.getName to $finalNewName")
      newFile
    }

    def editFile(file:File):Throwable\/Unit = \/.fromTryCatchNonFatal{
      val in = new FileInputStream(file)
      val str = String.valueOf(in.readAllBytes().map(_.toChar))

      val writer = new PrintWriter(file)
      writer.write(stringAdjustment(str))
      writer.close()
    }.leftMap(e => {
        System.err.println(s"Failed to read ${file.getCanonicalPath}. Exists? ${file.exists()} Read? ${file.canRead}")
        e.printStackTrace()
        e
    })


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
          if(name == CURR_SLOT_STR){
            val newCanonicalPath = file.getParentFile.getPath + File.separator + NEW_SLOT_STR
            val renamed = file.renameTo(new File(newCanonicalPath))
            println("::slot::"+renamed)
          } else if(DIFF_CHAR_CODE && name == OLD_CHAR_CODE) {
            val newCanonicalPath = file.getParentFile.getPath + File.separator + NEW_CHAR_CODE
            val renamed = file.renameTo(new File(newCanonicalPath))
            println("--code--"+renamed)
          }
        }

        val nextFileIter = for{
          rfile <- rfiles
          _ = println(rfile)
          parent = rfile.getParentFile
          if(parent != null)
          if(parent.getName() != "StreetFighterV")
        } yield parent
        println("+++++"+nextFileIter)
        folderConversions(nextFileIter)
      }
    }

    folderConversions(renamedFiles.map(_.getParentFile))
    println("Finished folder conversions")
  }




}
