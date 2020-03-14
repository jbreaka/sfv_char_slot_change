package jbreaka

import jbreaka.io.ByteArrayOps
import jbreaka.io.ByteArrayOps.Swap
import org.scalatest.funsuite.AnyFunSuite

class ByteArrayOpsTest  extends AnyFunSuite {
  val filler:Array[Byte] = Array[Byte](1,1,1,1,1)
  val pattern:Array[Byte] = Array[Byte](2,3,4,5,6)

  val replacement:Array[Byte] = Array[Byte](2,2,2,2,2)


  test("replace bytes") {
    val source:Array[Byte] = filler.
      concat(pattern).
      concat(filler.map(v => (v+2).toByte)).
      concat(pattern).
      concat(filler.map(v => (v+4).toByte))

    val expected:Array[Byte] = filler.
      concat(replacement).
      concat(filler.map(v => (v+2).toByte)).
      concat(replacement).
      concat(filler.map(v => (v+4).toByte))
    val result:Array[Byte] = ByteArrayOps.replaceAll(source,pattern,replacement)
    assertResult(expected)(result)
  }

  test("replace sequence bytes") {
    val swaps = List(Swap(pattern,replacement),Swap(filler,replacement))
    val source:Array[Byte] = filler.
      concat(pattern).
      concat(filler).
      concat(pattern).
      concat(filler)
    val expected:Array[Byte] = replacement.
      concat(replacement).
      concat(replacement).
      concat(replacement).
      concat(replacement)
    val result:Array[Byte] = ByteArrayOps.replaceAll(source,swaps)
    assertResult(expected)(result)
  }
}
