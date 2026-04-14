package lectures.part4implicits

object OrganizingImplicits extends App {

  implicit def reverseOrdering: Ordering[Int] = Ordering.fromLessThan(_ > _)
//  implicit val normalOrdering: Ordering[Int] = Ordering.fromLessThan(_ < _)
  println(List(1,4,5,3,2).sorted)

  // looks in scala.Predef

  /*
    implicits (used as implicit parameters)
      - val/var
      - object
      - accessor methods = defs with no parentheses
   */

  /*
    Exercise
   */
  case class Person(name: String, age: Int)

  val persons = List(
    Person("Steve", 30),
    Person("Amy", 22),
    Person("John", 66)
  )

//  object Person {
//    implicit val alphabeticOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
//  }

//  implicit val ageOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.age < b.age)
//  println(persons.sorted)

  /*
    implicit scope
      - normal scope = local scope (where we write code)
      - imported scope
      - companion objects of all types involved in the method signature
        - List
        - Ordering
        - all types involved = A or any supertype

      def sorted[B >: A](implicit ord: Ordering[B]): List[B]
   */

  object AlphabeticNameOrdering {
    implicit val alphabeticOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.name.compareTo(b.name) < 0)
  }

  object AgeOrdering {
    implicit val ageOrdering: Ordering[Person] = Ordering.fromLessThan((a, b) => a.age < b.age)
  }

//  import AlphabeticNameOrdering._
  import AgeOrdering._
  println(persons.sorted)

  /*
    Exercise
      - totalPrice = most used (50%)
      - by unit count = 25%
      - by unit price = 25%

      - define implicit orderings for purchases and organize them accordingly
   */
  case class Purchase(nUnits: Int, unitPrice: Double)

  object Purchase {
    implicit val totalPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan((a, b) => a.nUnits * a.unitPrice < b.nUnits * b.unitPrice)
  }

  object UnitCountOrdering {
    implicit val unitCountOrdering: Ordering[Purchase] = Ordering.fromLessThan(_.nUnits < _.nUnits)
  }

  object UnitPriceOrdering {
    implicit val unitPriceOrdering: Ordering[Purchase] = Ordering.fromLessThan(_.unitPrice < _.unitPrice)
  }


}
