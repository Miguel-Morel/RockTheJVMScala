package lectures.part2oop

import playground.{PrinceCharming, Cinderella => Princess}

import java.sql
//import playground._ <-- imports everything

import java.util.Date
import java.sql.{Date => SqlDate}

object PackagingAndImports extends App {

  // package members are accessible by their simple name
  val writer = new Writer("Daniel", "RockTheJVM", 2018)

  // import the package
  val princess = new Princess // playground.Cinderella = fully qualified name

  // packages are ordered hierarchically
  // matching folder structure

  // package object
  sayHello
  println(SPEED_OF_LIGHT)

  // imports
  val prince = new PrinceCharming

  val date = new Date
  // aliasing. fully qualified names also an option
  val sqlDate = new SqlDate(2018, 5, 4)

  // default import examples:
  // java.lang - String, Object, Exception
  // scala - Int, Nothing, Function
  // scala.Predef - println, ???
}
