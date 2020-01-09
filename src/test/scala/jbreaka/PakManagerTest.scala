package jbreaka

import java.io.File

import jbreaka.Main.pm
import jbreaka.capcom.CharacterCodes
import jbreaka.io.PakManager
import org.scalatest.funsuite.AnyFunSuite

class PakManagerTest extends AnyFunSuite {
  val pm = new PakManager()

  ignore("find the correct character codes") {
    val res = CharacterCodes.analyzePakPath("StreetFighterV/Content/Chara/Z33/SkelMesh/02/Texture/CT_Z33_02_COLOR_02.uasset")
    println(res)
  }

  ignore("read and extract pak files") {
    val u4pak = pm.writeU4pakToDisk()
    pm.read(u4pak, new File("/Volumes/Data/Development/devize/sfv_char_slot_change/src/test/resources/paks/PoisonC2-Catwoman.pak"))
  }
}
