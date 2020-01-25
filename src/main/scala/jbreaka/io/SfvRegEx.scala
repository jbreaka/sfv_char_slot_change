package jbreaka.io

import jbreaka.capcom.CharacterCodes
import jbreaka.capcom.CharacterCodes.SfvChar.SfvChar

import scala.util.matching.Regex
import scalaz._
import Scalaz._

object SfvRegEx {

  protected val characterCodePreix = "StreetFighterV/Content/Chara/".length
  protected val CHARACTER_CODE_REGEEX  = "(StreetFighterV|STREETFIGHTERV|streetfighterv)/Content/Chara/[a-zA-Z0-9]{3}".r

  def findSfvChar(str:String):Option[SfvChar]=for {
    s <- CHARACTER_CODE_REGEEX.findFirstIn(str)
    _ = println(s)
    characterCodeStr = s.substring(characterCodePreix,s.length)
    c <- CharacterCodes.getSfvChar(characterCodeStr)
  } yield c

  protected val SLOT_REGEEX  = "(StreetFighterV|STREETFIGHTERV|streetfighterv)/Content/Chara/[a-zA-Z0-9]{3}/[a-zA-Z]*/\\d{2}".r
  protected val SLOT_EXTRACT_REGEX = "/\\d{2}".r

  def findSlot(content:String):Option[Short]= for {
    s <- SLOT_REGEEX.findFirstIn(content)
    iter = SLOT_EXTRACT_REGEX.findAllIn(s)
    slot = iter.group(iter.groupCount).substring(1)
  } yield slot.toShort

  def strFromSlot(slot:Short):String= if(slot < 10) s"0${slot}" else slot+""

  protected val REPLACE_REGEEX  = "(StreetFighterV|STREETFIGHTERV|streetfighterv)/Content/Chara/[a-zA-Z0-9]{3}/[a-zA-Z]*/\\d{2}".r


  def replaceStrings(char:SfvChar, newSlot:Short, prevSlot:Short, str:String):String={

    case class Change(regEx:Regex,replace:String){
      def change(str:String):String = {
        println(s"replacing ${regEx.pattern} to $replace ...")
        regEx.replaceAllIn(str,replace)
      }
    }

    val result = for{
      newCharCode  <- CharacterCodes.getSfvCharCode(char,newSlot) \/> "Couldn't find new character code."
      prevCharCode <- CharacterCodes.getSfvCharCode(char,prevSlot) \/> "Couldn't find old character code."

      newSlotStr = strFromSlot(newSlot)
      prevSlotStr =  strFromSlot(prevSlot)
      _ = println(s"${prevCharCode}_${prevSlotStr} becomes ${newCharCode}_${newSlotStr}")
    } yield {
      //These will be applied in sequence
      val CHANGES = List(
        Change("(StreetFighterV|STREETFIGHTERV|streetfighterv)/Content".r,"StreetFighterV/Content"),
        Change(s"${prevCharCode}_${prevSlotStr}".r,s"${newCharCode}_${newSlotStr}"),
        Change(s"_${prevCharCode}_".r,s"_${newCharCode}_"),
        Change(s"/$prevSlotStr/".r,s"/$newSlotStr/"),
        Change(s"/$prevCharCode".r,s"/$newCharCode")
      ) ++
        List[String]("Costume","Preview","Setting","Preset","Material","Prop").
          map(k => Change((s"${k}_${prevSlotStr}").r,s"${k}_${newSlotStr}"))
        CHANGES.foldLeft(str)((s:String,c:Change)=> c.change(s))
    }
    result match {
      case -\/(issue) =>
        println(issue)
        str
      case \/-(newStr) => newStr
    }
  }
}
