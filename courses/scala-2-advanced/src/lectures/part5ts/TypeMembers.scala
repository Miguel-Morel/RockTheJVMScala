package lectures.part5ts

object TypeMembers extends App {

  class Animal
  class Dog extends Animal
  class Cat extends Animal

  class AnimalCollection {
    type AnimalType // abstract type member
    type BoundedAnimal <: Animal
    type SuperBoundedAnimal >: Dog <: Animal
    type AnimalC = Cat
  }

  val ac = new AnimalCollection
//  val dog: ac.AnimalType = ???
//  val cat: ac.BoundedAnimal = new Cat <- compiler doesn't know what BoundedAnimal is, so it shows an error
  val pup: ac.SuperBoundedAnimal = new Dog
  val cat: ac.AnimalC = new Cat

  type CatAlias = Cat
  val anotherCat: CatAlias = new Cat

  // alternative to generics
  trait MyList {
    type T
    def add(element: T): MyList
  }

  class NonEmptyList(value: Int) extends MyList {
    override type T = Int
    def add(element: Int): MyList = ???
  }

  // .type
  type CatsType = cat.type
  val newCat: CatsType = cat
//  new CatsType // <- error: class type required but lectures.part5ts.TypeMembers.cat.type found

  /*
    exercise

    enforce a type to be applicable to _some_ types only
   */

  // trait MList locked - someone else wrote this
  trait MList {
    type A
    def head: A
    def tail: MList
  }

  // all numbers are under the Number type
  // use type members and type member constraints

  // this solution makes CustomList not compile
  trait ApplicableToNumbers {
    type A <: Number
  }

  // should not be ok
  class CustomList(hd: String, t1: CustomList) extends MList with ApplicableToNumbers {
    type A = String
    def head = hd
    def tail = t1
  }

  // should be ok
  class IntList(hd: Int, t1: IntList) extends MList {
    type A = Int
    def head = hd
    def tail = t1
  }





}
