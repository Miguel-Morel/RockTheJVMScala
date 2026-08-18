package com.rockthejvm.part1changes

object MinorChanges {

  // importing everything
  import scala.concurrent.duration.* // * instead of _

  // alias
  import java.util.{List => JList} // scala 2 notation
  import java.util.List as JList // scala 3 style

  // import everything BUT something
  import java.util.{List as _ /* <-- import to be ignored */, *}

  // variable arguments
  val aList = List(1,2,3,4)

  // many collections have vararg-apply methods. we might sometimes want to expand an existing collection to varargs
  val anArray = Array(aList(0), aList(1), aList(2), aList(3))

  // scala 2
  val anArray_v2 = Array(aList: _*)

  // scala 3
  val anArray_v3 = Array(aList*) // consistent with the vararg pattern match

  // trait constructor arguments
  trait Person(name: String) // legal now
//  trait Kid extends Person("Alice") <- illegal
//  class Kid extends Person("Alice") <- legal

  // solve the diamond problem
//  trait JPerson extends Person("John")
  ////  trait APerson extends Person("Alice")
  ////  trait Kid extends JPerson with APerson // kinda weird, not clear which constructor argument would prevail

  // universal constructors == apply methods everywhere
  class Pet(name: String)
  val lassie = new Pet("Lassie") // scala 2
  val lassie_v2 = Pet("Lassie") // scala 3 

}
