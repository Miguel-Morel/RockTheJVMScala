package exercises

import lectures.part4implicits.TypeClasses.{Equal, User, john}

object EqualityPlayground extends App {

  // equality
  trait Equal[T] {
    def apply(a: T, b: T): Boolean
  }

  implicit object NameEquality extends Equal[User] {
    override def apply(a: User, b: User): Boolean = a.name == b.name
  }

  object FullEquality extends Equal[User] {
    override def apply(a: User, b: User): Boolean = a.name == b.name && a.email == b.email
  }

  object Equal {
    def apply[T](a: T, b: T)(implicit equalizer: Equal[T]): Boolean =
      equalizer.apply(a, b)
  }

  val anotherJohn = User("John", 45, "anotherJohn@rockthejvm.com")

  val john = User("John", 32, "john@rockthejvm.com")
  // ad-hoc polymorphism
  println(Equal(john, anotherJohn))

  /*
    Exercise

   1. improve the Equal type class with an implicit conversion class
      ===(another value: T)
   2. !==(another value: T)
   */

  implicit class TypeSafeEqual[T](value: T) {
    def ===(other: T)(implicit equalizer: Equal[T]): Boolean = equalizer(value, other)
    def !==(other: T)(implicit equalizer: Equal[T]): Boolean = !equalizer(value, other)
  }

  /*
    john.===(anotherJohn)
    new TypeSafeEqual[User](john).===(anotherJohn)
    new TypeSafeEqual[User](john).===(anotherJohn)(NameEquality)

    TYPE-SAFE
   */
//  println(john === 43) // TYPE-SAFE, disallows invalid comparison
  println(john === anotherJohn)


}
