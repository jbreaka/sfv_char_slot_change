package jbreaka

import java.io.File

import jbreaka.capcom.CharacterCodes
import jbreaka.io.PakManager
import org.scalatest.FunSuite

class PakManagerTest extends FunSuite {
  val pm = new PakManager()

  test("find the correct character codes") {
    val res = CharacterCodes.analyzePakPath("StreetFighterV/Content/Chara/Z33/SkelMesh/02/Texture/CT_Z33_02_COLOR_02.uasset")
    println(res)
  }

  test("read and extract pak files") {
    pm.read(new File("/Volumes/Data/Development/devize/sfv_char_slot_change/src/test/resources/paks/PoisonC2-Catwoman.pak"))
  }
}
