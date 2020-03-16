package jbreaka.io

import scala.annotation.tailrec

object ByteArrayOps {

  @tailrec
  def replaceAll(source:Array[Byte], pattern:Array[Byte],replacement:Array[Byte],offset:Int=0):Array[Byte]={
    val index = source.indexOfSlice(pattern,offset)
    if(index < 0) source
    else {
      val prefix = source.slice(0,index)
      val suffix = source.drop(index + pattern.length)
      replaceAll(prefix ++ replacement ++ suffix, pattern, replacement, index+replacement.length)
    }
  }

  case class Swap(patterns:Array[Byte],replacement:Array[Byte])
  def replaceAll(source:Array[Byte], swaps:List[Swap]):Array[Byte]={
    println("Swapping all references within the PAK contents")
    val TOTAL = swaps.size.toDouble

    swaps.foldLeft(source)((src,swap)=>{
      val index = swaps.indexOf(swap).toDouble
      val percentage = ((index/TOTAL)*100).toInt
      val res = replaceAll(src,swap.patterns,swap.replacement)
      print(s" ${percentage}% done \r")
      res
    })
  }
}
