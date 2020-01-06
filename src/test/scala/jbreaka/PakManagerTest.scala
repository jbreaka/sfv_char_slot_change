package jbreaka

import java.io.File

import jbreaka.io.PakManager
import org.scalatest.FunSuite

class PakManagerTest extends FunSuite {
  val pm = new PakManager()

  test("find the correct character codes") {

    assert(pm.charCodes.size == 38)
    assert(pm.charCodes.contains("URN"))
  }

  test("read and extract pak files") {
    pm.read(new File("/Volumes/Data/Development/devize/sfv_char_slot_change/src/test/resources/paks/PoisonC2-Catwoman.pak"))
  }
}
