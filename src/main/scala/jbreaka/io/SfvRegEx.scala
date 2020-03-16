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

  case class Change(regEx:Regex,replace:String,oddity:Boolean=false){
    def change(str:String):String = {
      println(s"replacing ${regEx.pattern} to $replace ...")
      regEx.replaceAllIn(str,replace)
    }
  }

  def getMagicNumberSequence(newSlot:Short, prevSlot:Short):(Array[Byte],Array[Byte]) = {
    def getVal(slot:Short):Byte= {
      /*00 = C1 to 9
      0B = C10
      0C = C11
      0D = C12
      0E = C13
      0F = C14
      10 = C15
      11 = C16
      12 = C17
      13 = C18
      14 = C19
      15 = C20
      16 = C21
      17 = C22
      18 = C23
      19 = C24
      20 = C25
      21 = C26
      22 = C27
      23 = C28
      24 = C29
      25 = C30*/
      val res = if(slot < 10) 0
      else if(slot < 25)(slot+1)
      else (slot+7)
      res.toByte
    }
    val SUFFIX : List[Byte] = List[Int](0x00, 0x00, 0x00, 0x0B).map(_.toByte)
    val prev = (getVal(prevSlot) :: SUFFIX).toArray
    val next = (getVal(newSlot) :: SUFFIX).toArray
    (prev,next)
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
        Change(s"${prevCharCode}_".r,s"${newCharCode}_")
      ) ++
      changeCodes ++
      List[String]("Costume","Preview","Setting","Preset","Material","Prop").
          map(k => Change((s"${k}_${prevSlotStr}").r,s"${k}_${newSlotStr}")) ++
      List(
        Change(s"/$prevSlotStr/".r,s"/$newSlotStr/"),
        Change(s"/$prevCharCode[^/]".r,s"/$newCharCode",true),
      )
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
      //we want to avoid /CharCode/ folder being changed, so this helps filter out the problem
      replacementStr = if(change.oddity) change.replace + mtch.last else change.replace
      _ = println(s"$mtch  -->  ${replacementStr}")
    } yield Swap(mtch.getBytes,replacementStr.getBytes)).run
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
