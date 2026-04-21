package part1intro

object CatsIntro {

  // eq
//  val aComparison = 2 == "a string" -> wrong. will trigger a compiler warning. will always return false

  // part 1 - type class import
  import cats.Eq

  // part 2 - import type class instances for the types needed
  import cats.instances.int._

  // part 3 - use the type class API
  val intEquality = Eq[Int]

  val aTypeSafeComparison = intEquality.eqv(2, 3) // false
//  val anUnsafeComparison = intEquality.eqv(2, "a string") -> doesn't compile (type mismatch)

  // part 4 - use extension methods (if applicable)
  import cats.syntax.eq._
  val anotherTypeSafeComparison = 2 === 3 // false
  val notEqualComparison = 2 =!= 3 // true
//  val invalidComparison = 2 === "a String" -> doesn't compile (type mismatch)

  // extension methods are only visible in the presence of the right type class instances

  // part 5 - extending the type class operations to composite types
  val aListComparison = List(2) === List(3) // returns false

  // part 6 - create a type class instance for a custom type
  case class ToyCar(model: String, price: Double)

  implicit val toyCarEq: Eq[ToyCar] = Eq.instance[ToyCar] { (car1, car2) =>
    car1.price == car2.price
  }
  
  val compareTwoToyCars = ToyCar("Ferrari", 29.99) === ToyCar("Lamborghini", 29.99) // true

}
