package jbreaka

import jbreaka.io.SfvRegEx
import org.scalatest.funsuite.AnyFunSuite
import scalaz._
import Scalaz._
import jbreaka.io.FileManager
import jbreaka.capcom.CharacterCodes.SfvChar
import java.io.File

class SfvRegExTest  extends AnyFunSuite {
  ignore("find character code") {
    assert(SfvRegEx.findSfvChar("StreetFighterV/Content/Chara/Z33/SkelMesh/02/Material/CM_Z33_02_E15_Whip_L.uasset").isDefined)
    assert(SfvRegEx.findSfvChar("streetfighterv/Content/Chara/Z33/SkelMesh/02/Material/CM_Z33_02_E15_Whip_L.uasset").isDefined)
    assert(SfvRegEx.findSfvChar("STREETFIGHTERV/Content/Chara/Z33/SkelMesh/02/Material/CM_Z33_02_E15_Whip_L.uasset").isDefined)
  }

  ignore("find character slot") {
    assert(SfvRegEx.findSlot("StreetFighterV/Content/Chara/Z33/SkelMesh/02/Material/CM_Z33_02_E15_Whip_L.uasset") == 2.toShort.some)
    assert(SfvRegEx.findSlot("streetfighterv/Content/Chara/Z33/SkelMesh/02/Material/CM_Z33_02_E15_Whip_L.uasset") == 2.toShort.some)
    assert(SfvRegEx.findSlot("STREETFIGHTERV/Content/Chara/Z33/SkelMesh/02/Material/CM_Z33_02_E15_Whip_L.uasset") == 2.toShort.some)
  }

  test("replace strings"){
    val pak2Conv = new File("src/test/resources/paks/[Necalli][C1]Venom - Marvel.pak")
//    val str = FileManager.fileToString(pak2Conv)
    val str =
      """afsdasfdfs lkfdjsalkfjdsklfjadklfajsklsfa
        |streetfighterv/Content/Chara/BLR/SkelMesh/02/Material/CM_BLR_02_E15_Whip_L.uasset
        |STREETFIGHTERV/Content/Chara/BLR/SkelMesh/02/Material/CM_BLR_02_E15_Whip_L.uasset
        |""".stripMargin
    val expected =
      """afsdasfdfs lkfdjsalkfjdsklfjadklfajsklsfa
      |StreetFighterV/Content/Chara/Z08/SkelMesh/05/Material/CM_Z08_05_E15_Whip_L.uasset
      |StreetFighterV/Content/Chara/Z08/SkelMesh/05/Material/CM_Z08_05_E15_Whip_L.uasset
      |""".stripMargin
//    println(str)
    val result = SfvRegEx.replaceStrings(SfvChar.Claws,5,2,str)
    println("result::\n"+result)
    println("expected::\n"+expected)
    assert(result == expected)
  }

  test("test'n regex"){
    import scala.util.matching._
    import scala.util.matching.Regex._

    val codeR = "/CNL[^/]".r
    val str = """/CNL123  /CNL/  /CNL_12"""
    val resList = codeR.findAllIn(str).toList
    assert(resList.size == 2,s"size was ${resList.size} but should be 2")
    resList.foreach(println)
  }
}
