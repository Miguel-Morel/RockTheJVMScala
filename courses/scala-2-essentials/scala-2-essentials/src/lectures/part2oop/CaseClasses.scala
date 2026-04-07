package lectures.part2oop

object CaseClasses extends App {

  /*
    equals, hashCode, toString
   */

  case class Person(name: String, age: Int)

  // 1. promotes class parameters to fields
  val jim = new Person("Jim", 34)
  println(jim.name)

  // 2. sensible toString
  // println(instance) = println(instance.toString) <-- syntactic sugar
  println(jim)

  // 3. equals and hashCode implemented out of the box (ootb)
  val jim2 = new Person("Jim", 34)
  println(jim == jim2)

  // 4. handy copy method
  val jim3 = jim.copy(age = 45)

  // 5. included companion objects
  val thePerson = Person
  val mary = Person("Mary", 23)

  // 6. serializable
  //example: Akka messages (done in case classes)

  // 7. have extractor patterns = can be used in pattern matching
  //case objects inherit all functionality from case classes, except having companion objects, as they themselves are the companion objects
  case object UnitedKingdom {
    def name: String = "The UK of GB and NI"
  }

  /*
    Expand MyList - use case classes and case objects
   */

}
