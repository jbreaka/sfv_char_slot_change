package jbreaka.capcom

import scala.io.Source
import scalaz._
import Scalaz._

import scala.annotation.tailrec

object CharacterCodes {

  object SfvChar extends Enumeration {
    type SfvChar = Value
    val Ryu, ChunLi, Nash, Dictator, Cammy, Birdie, Ken, Necalli, Claws, RMika, Rashid,
    Karin, Zangief, Laura, Dhalsim, FANG, Alex, Guile, Ibuki, Boxer, Juri, Urien, Akuma,
    Kolin, Ed, Abigail, Menat, Zeku, Sakura, Blanka, Falke, Cody, GPres, Sagat, Kage,
    Poison, EHonda, Lucia, Gil, Seth = Value
  }
  import SfvChar._

  def getSfvChar(code:String):Option[SfvChar]= Option(code match {
    case "RYU" => Ryu
    case "Z00" => Ryu
    case "CNL" => ChunLi
    case "Z02" => ChunLi
    case "NSH" => Nash
    case "Z07" => Nash
    case "VEG" => Dictator
    case "Z01" => Dictator
    case "CMY" => Cammy
    case "Z10" => Cammy
    case "BRD" => Birdie
    case "Z0A" => Birdie
    case "KEN" => Ken
    case "Z03" => Ken
    case "NCL" => Necalli
    case "Z0F" => Necalli
    case "BLR" => Claws
    case "Z08" => Claws
    case "RMK" => RMika
    case "Z0B" => RMika
    case "RSD" => Rashid
    case "Z0C" => Rashid
    case "KRN" => Karin
    case "Z04" => Karin
    case "ZGF" => Zangief
    case "Z05" => Zangief
    case "LAR" => Laura
    case "Z0E" => Laura
    case "DSM" => Dhalsim
    case "Z06" => Dhalsim
    case "FAN" => FANG
    case "Z0D" => FANG
    case "ALX" => Alex
    case "Z15" => Alex
    case "GUL" => Guile
    case "Z11" => Guile
    case "IBK" => Ibuki
    case "Z12" => Ibuki
    case "BSN" => Boxer
    case "Z13" => Boxer
    case "JRI" => Juri
    case "Z09" => Juri
    case "URN" => Urien
    case "Z14" => Urien
    case "Z21" => Akuma
    case "Z20" => Kolin
    case "Z22" => Ed
    case "Z24" => Abigail
    case "Z23" => Menat
    case "Z25" => Zeku
    case "Z26" => Sakura
    case "Z27" => Blanka
    case "Z28" => Falke
    case "Z29" => Cody
    case "Z30" => GPres
    case "Z31" => Sagat
    case "Z32" => Kage
    case "Z33" => Poison
    case "Z34" => EHonda
    case "Z35" => Lucia
    case "Z36" => Gil
    case "Z37" => Seth
    case _ => null
  })

  def getSfvCharCode(pak:Pak):Option[String]= getSfvCharCode(pak.character,pak.slot)
  def getSfvCharCode(sfvChar:SfvChar,slot:Short):Option[String] = Option(sfvChar match {
    case Ryu => if(slot < 9) "RYU" else "Z00"
    case ChunLi =>  if(slot <=5) "CNL"
                    else if (slot >=6 && slot <=7) "Z02"
                    else if (slot >=8 && slot <=10) "CNL"
                    else if (slot ==11 ) "Z02"
                    else if (slot >=12 && slot <=14) "CNL"
                    else "Z02"
    case Nash => if(slot <= 4) "NSH"  else "Z07"
    case Dictator => if(slot <= 4) "VEG" else "Z01"
    case Cammy => if(slot <= 9) "CMY" else "Z10"
    case Birdie => if(slot <= 3) "BRD" else "Z0A"
    case Ken => if(slot <= 4) "KEN" else "Z03"
    case Necalli => if(slot <= 5) "NCL" else "Z0F"
    case Claws => if(slot <= 4)"BLR" else "Z08"
    case RMika => if(slot <= 7 ) "RMK" else "Z0B"
    case Rashid => if(slot <= 3 ) "RSD" else "Z0C"
    case Karin => if(slot <= 8) "KRN" else "Z04"
    case Zangief => if(slot <= 5) "ZGF" else "Z05"
    case Laura => if(slot <=7) "LAR" else "Z0E"
    case Dhalsim => if(slot <=3) "DSM" else "Z06"
    case FANG => if(slot <=4) "FAN" else "Z0D"
    case Alex => if(slot <= 5) "ALX" else "Z15"
    case Guile => if(slot <= 3) "GUL" else "Z11"
    case Ibuki => if(slot <=4 || slot == 8) "IBK" else "Z12"
    case Boxer => if(slot <= 3) "BSN" else "Z13"
    case Juri => if(slot <= 6) "JRI" else "Z09"
    case Urien => if(slot <= 4) "URN" else "Z14"
    case Akuma => "Z21"
    case Kolin => "Z20"
    case Ed => "Z22"
    case Abigail => "Z24"
    case Menat => "Z23"
    case Zeku => "Z25"
    case Sakura => "Z26"
    case Blanka => "Z27"
    case Falke => "Z28"
    case Cody => "Z29"
    case GPres => "Z30"
    case Sagat => "Z31"
    case Kage => "Z32"
    case Poison => "Z33"
    case EHonda => "Z34"
    case Lucia => "Z35"
    case Gil => "Z36"
    case Seth => "Z37"
    case _ => null
  })

  case class Pak(character:SfvChar,slot:Short,code:String){
    require(code.size == 3)
    require(slot > 0)
    require(character != null)
  }

  /**
   * Get the Character and the Slot of the existing PAK
   */
  def analyzePakPath(path:String):Option[Pak] ={
    val PATH_SIZE = path.size
    println(s"analyze $path")
    /**
     * get the slot of the PAK
     */
    @tailrec
    def findSlot(start:Int):Option[Short] = {
      if( start < 0 || start >= PATH_SIZE-1) None
      else {
        val endIndex = path.indexOf('/',start+1)
        val sub = path.substring(start, endIndex).replaceAll("/","")
        val resO = try {
          println("trying...."+sub)
          sub.toShort.some
        } catch {
          case n:NumberFormatException => None
          case e:Exception => e.printStackTrace()
        }
        resO match {
          case None => findSlot(endIndex)
          case s:Some[Short] => s
        }
      }
    }

    val PREFIX = "StreetFighterV/Content/Chara/"
    path.indexOf(PREFIX).some.filter(_ >= 0).
      flatMap(index=> {
        val endCharCode = path.indexOf('/',index+PREFIX.length+1)
        println(s"&&& index: $index  endCharCode: $endCharCode")
        val characterCode:String = path.substring(index+PREFIX.size,endCharCode)
        println(">>>"+characterCode)
        val slotO = findSlot(endCharCode)
        println(s"slot = $slotO")
        slotO.map(slot => (characterCode,slot))
      }).flatMap(pair => {
        getSfvChar(pair._1).
          flatMap(sfvC => getSfvCharCode(sfvC, pair._2).map(c => Pak(sfvC, pair._2, c)))
      })
  }

}
