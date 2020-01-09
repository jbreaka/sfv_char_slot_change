package jbreaka

import com.typesafe.config.ConfigFactory
import org.scalatest.funsuite.AnyFunSuite

class MainTest extends AnyFunSuite {

  ignore("An empty Set should have size 0") {
    assert(Set.empty.size == 0)
    val conf = ConfigFactory.load();
    val bar1 = conf.getString("testfile.pak");
    println(bar1)
  }

  ignore("Invoking head on an empty Set should produce NoSuchElementException") {
    assertThrows[NoSuchElementException] {
      Set.empty.head
    }
  }
}