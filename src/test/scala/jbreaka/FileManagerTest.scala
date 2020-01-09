package jbreaka

import java.io.File

import jbreaka.capcom.CharacterCodes.{Pak, SfvChar}
import jbreaka.io.FileManager
import org.scalatest.funsuite.AnyFunSuite

class FileManagerTest extends AnyFunSuite {
  ignore("name correction") {
    val fm = new FileManager()
    val files = Set(
      new File("StreetFighterV/Content/Chara/Z33/SkelMesh/02/Material/CM_Z33_02_E15_Whip_L.uasset"),
      new File("StreetFighterV/Content/Chara/Z33/SkelMesh/02/Texture/CT_Z33_02_MASK_03.uasset"),
      new File("StreetFighterV/Content/Chara/Z33/SkelMesh/02/DataAsset/DA_Z33_CustomizeSetting_02.uasset"),
      new File("StreetFighterV/Content/Chara/Z33/SkelMesh/02/Material/CM_Z33_02_E06_ChokerShortbelt.uasset")
    )
    println("^^^^^^^^^^^^^^^")
    files.map(_.getName).foreach(println)
    println("vvvvvvvvvvvvvvv")
    fm.manage(Pak(SfvChar.Poison,2,"Z33"), 3, files)
    assert(false)
  }
}
