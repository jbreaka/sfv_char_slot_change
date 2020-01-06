package jbreaka
import com.typesafe.config.ConfigFactory

object Main extends App {
  println("moo")

  val conf = ConfigFactory.load();
  val bar1 = conf.getString("testfile.pak");
  println(bar1)
}
