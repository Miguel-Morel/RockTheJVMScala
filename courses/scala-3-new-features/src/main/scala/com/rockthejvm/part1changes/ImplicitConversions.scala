package com.rockthejvm.part1changes

object ImplicitConversions {

  case class Person(name: String) {
    def greet: String = s"hey my name is $name, scala rocks"
  }

  // scala 2
//  implicit def string2Person(string: String): Person = Person(string)
//
//  // implicit conversions in this style are discouraged
//  val daniel: Person = "Daniel" // string2Person("Daniel")
//  "Daniel".greet //  sring2Person("Daniel").greet

  // scala 3: add implicit conversions explicitly

  // step 1: import the implicit conversions support
  import scala.language.implicitConversions

  // step 2: define a given value of type Conversion
  given string2Person: Conversion[String, Person] with
    override def apply(string: String): Person = Person(string)

  // 1 - use methods of the converted type
    "Daniel".greet

  // 2 - use the convertee instead of the required type
  val person: Person = "Daniel"

  def sayHiTo(perosn: Person): Unit =
    println(s"hi, ${person.name}")

  sayHiTo("Alice") // ok

  def main(args: Array[String]): Unit = {

  }

}
