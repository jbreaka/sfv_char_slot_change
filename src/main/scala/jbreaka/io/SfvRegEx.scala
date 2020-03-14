package jbreaka.io

import jbreaka.capcom.CharacterCodes
import jbreaka.capcom.CharacterCodes.SfvChar.SfvChar

import scala.util.matching.Regex
import scalaz._
import Scalaz._
import jbreaka.io.ByteArrayOps.Swap

import scala.util.matching.Regex.Match

object SfvRegEx {

  protected val characterCodePreix = "StreetFighterV/Content/Chara/".length
  protected val CHARACTER_CODE_REGEEX  = "(StreetFighterV|STREETFIGHTERV|streetfighterv)/Content/Chara/[a-zA-Z0-9]{3}".r

  def findSfvChar(str:String):Option[SfvChar]=for {
    s <- CHARACTER_CODE_REGEEX.findFirstIn(str)
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

  case class Change(regEx:Regex,replace:String){
    def change(str:String):String = {
      println(s"replacing ${regEx.pattern} to $replace ...")
      regEx.replaceAllIn(str,replace)
    }
  }

  /**
   * Get the changes that need to be made
   * @param char
   * @param newSlot
   * @param prevSlot
   * @return
   */
  def getChanges(char:SfvChar, newSlot:Short, prevSlot:Short):String\/List[Change]={
    for{
      newCharCode  <- CharacterCodes.getSfvCharCode(char,newSlot) \/> "Couldn't find new character code."
      prevCharCode <- CharacterCodes.getSfvCharCode(char,prevSlot) \/> "Couldn't find old character code."

      newSlotStr = strFromSlot(newSlot)
      prevSlotStr =  strFromSlot(prevSlot)
      _ = println(s"${prevCharCode}_${prevSlotStr} becomes ${newCharCode}_${newSlotStr}")
    } yield {
      val changeCodes = if(prevCharCode == newCharCode) Nil else Change(s"_${prevCharCode}_".r,s"_${newCharCode}_")::Nil
      //These will be applied in sequence
      List(
        Change("(StreetFighterV|STREETFIGHTERV|streetfighterv)/Content".r,"StreetFighterV/Content"),
        Change(s"${prevCharCode}_${prevSlotStr}".r,s"${newCharCode}_${newSlotStr}"),
      ) ++
      changeCodes ++
      List(
        Change(s"/$prevSlotStr/".r,s"/$newSlotStr/"),
        Change(s"/$prevCharCode".r,s"/$newCharCode")
      ) ++
        List[String]("Costume","Preview","Setting","Preset","Material","Prop").
          map(k => Change((s"${k}_${prevSlotStr}").r,s"${k}_${newSlotStr}"))
    }
  }

  /**
   * Determine which strings within the PAK need to be replaced and with what.  This returns that replacement/swap
   * pairing.
   */
  def identifySwaps(char:SfvChar, newSlot:Short, prevSlot:Short, content:String):Exception\/List[Swap]={
    type SV[+A] = Exception \/ A
    implicit object SVFunction extends Functor[SV]{
      override def map[A, B](fa: SV[A])(f: A => B): SV[B] = fa.map(f)
    }

    (for{
      change <-  ListT[SV,Change](getChanges(char, newSlot, prevSlot).leftMap(s => new Exception(s)))
      _ = println(change)
      matched:SV[List[String]] = change.regEx.findAllIn(content).toSet.toList.right[Exception]
      mtch <- ListT[SV,String](matched)
      _ = println(s"$mtch  -->  ${change.replace}")
    } yield Swap(mtch.getBytes,change.replace.getBytes)).run
  }

  def replaceStrings(char:SfvChar, newSlot:Short, prevSlot:Short, str:String):String={
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
