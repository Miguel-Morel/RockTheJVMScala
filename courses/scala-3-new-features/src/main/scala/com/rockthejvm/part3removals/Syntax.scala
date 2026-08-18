package com.rockthejvm.part3removals

object Syntax {

  // scala 2

  // do-while instruction - not supported by scala 3
//  var i = 0
//  do {
//    println(i)
//    i += 1
//  } while (i < 10)

//  // methods returning Unit without the = sign was removed (procedural syntax)
//  def sayHi() {
//    // some block returning Unit
//  }

  // argument limit of 22 has been removed in scala 3

  // methods with no arguments vs methods with an empty argument list
  def aParamaterlessMethod = 42
  def aMethodWithEmptyArgList() = 42

  // in scala 2, you can call both with the "parameterless" syntax
  val meaningOfLife = aParamaterlessMethod // ok in both
//  val meaningOfLife_v2 = aMethodWithEmptyArgList // ok, with warning (scala 2), NOT ok in scala 3
  // not the other way around
//  val meaningOfLife_v3 = aParamaterlessMethod() // illegal

  // uninitialized vars
  // scala 2
  var toAssignLater: Int = _ // ok, but will be phased out in future scala 3 versions
  // some time later
  toAssignLater = 87 // ok

  // scala 3 style
  import scala.compiletime.uninitialized
  var toBeSetLater: Int = uninitialized
  // set it later
  toBeSetLater = 68



  def main(args: Array[String]): Unit = {
    println(
      s"""
        | val functionWithLotsOfArgs = (
        | ${(1 to 25).map(i => s"x${i}: Int").mkString(",\n")}
        |) => println("lots of arguments")
        |""".stripMargin)
  }

}
