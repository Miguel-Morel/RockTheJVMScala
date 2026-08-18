package com.rockthejvm.part2additions

import scala.annotation.targetName

object InfixNotation {

  case class Person(name: String) {
    infix /* <-- scala 3 only */ def likes(movie: String): String = s"$name likes $movie"
    @targetName("amazedBy") // should be an alphanumeric token
    infix def !!(observation: String): String = s"wow $observation"
  }

  // scala 2
  val person = Person("Alice")
  person.likes("Forrest Gump")
  person likes "Forrest Gump" // infix notation - for methods with a single argument

  // scala 3 - explicit with the 'infix' modifier
  // infix is a 'soft' modifier (not enforced + does not collide with variable names)
  val infix = 2 // ok

  // extension methods can also be infix
  extension (person: Person)
    infix def enjoys(musicGenre: String): String = s"${person.name} listens to $musicGenre"

  person enjoys "classical music" // ok

  /*
    target name - rename "operator" methods to java-legal names
    - new method name can be called from java
    - new method name cannot be called from scala (rather use the original name)
   */
  def main(args: Array[String]): Unit = {
//    println(person.amazedBy("scala 3")) <-- doesn't work. amazedBy doesn't exist on the Person type
    println(person.!!("scala 3")) // ok
  }

  // infix types
  infix trait <:>[A, B]
  type Ints = Int <:> Int

}
