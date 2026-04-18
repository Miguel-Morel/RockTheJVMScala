package lectures.part5ts

object FBoundedPolymorphism extends App {

  // how do I force a method in a super type to accept a current type?
//  trait Animal {
//    def breed: List[Animal]
//  }
//
//  class Cat extends Animal {
//    override def breed: List[Animal] = ??? // want List[Cat]
//  }
//
//  class Dog extends Animal {
//    override def breed: List[Animal] = ??? // want List[Dog]
//  }

  // solution 1 - naive
//  trait Animal {
//    def breed: List[Animal]
//  }
//
//  class Cat extends Animal {
//    override def breed: List[Cat] = ??? // want List[Cat]
//  }
//
//  class Dog extends Animal {
//    override def breed: List[Cat] = ??? // want List[Dog]
//  }

  // solution 2 - fbp
//  trait Animal[A <: Animal[A]] { // recursive type: f-bounded polymorphism
//    def breed: List[Animal[A]]
//  }
//
//  class Cat extends Animal[Cat] {
//    override def breed: List[Cat] = ??? // want List[Cat]
//  }
//
//  class Dog extends Animal[Dog] {
//    override def breed: List[Animal[Dog]] = ??? // want List[Dog]
//  }
//
//  trait Entity[E <: Entity[E]] // ORM
//
//  class Person extends Comparable[Person] { // fbp
//    override def compareTo(o: Person): Int = ???
//  }
//
//  class Crocodile extends Animal[Dog] {
//    override def breed: List[Animal[Dog]] = ???
//  }

  // solution 3 - fbp + self-types

//  trait Animal[A <: Animal[A]] { self: A =>
//    def breed: List[Animal[A]]
//  }
//
//  class Cat extends Animal[Cat] {
//    override def breed: List[Cat] = ??? // want List[Cat]
//  }
//
//  class Dog extends Animal[Dog] {
//    override def breed: List[Animal[Dog]] = ??? // want List[Dog]
//  }

//  class Crocodile extends Animal[Dog] {
//    override def breed: List[Animal[Dog]] = ???
//  }

//  trait Fish extends Animal[Fish]
//
//  class Shark extends Fish {
//    override def breed: List[Animal[Fish]] = List(new Cod) // wrong
//  }
//
//  class Cod extends Fish {
//    override def breed: List[Animal[Fish]] = ???
//  }

  /*
    Exercise
   */

  // solution 4 - type classes

//  trait Animal
//  trait CanBreed[A] {
//    def breed(a: A): List[A]
//  }
//
//  class Dog extends Animal
//  object Dog {
//    implicit object DogsCanBreed extends CanBreed[Dog] {
//      def breed(a: Dog): List[Dog] = List()
//    }
//  }
//
//  implicit class CanBreedOps[A](animal: A) {
//    def breed(implicit canBreed: CanBreed[A]): List[A] = {
//      canBreed.breed(animal)
//    }
//  }
//
//  val dog = new Dog
//  dog.breed // List[Dog]
//  /*
//    new CanBreedOps[Dog](dog).breed(Dog.DogsCanBreed)
//
//    implicit value to pass to breed: Dog.DogsCanBreed
//   */
//
//  class Cat extends Animal
//  object Cat {
//    implicit object CatsCanBreed extends CanBreed[Dog] {
//      def breed(a: Dog): List[Dog] = List()
//    }
//  }
//
//  val cat = new Cat
//  cat.breed // wrong. missing right implicit

  // solution 5

  trait Animal[A] { // pure type classes
    def breed(a: A): List[A]
  }

  class Dog
  object Dog {
    implicit object DogAnimal extends Animal[Dog] {
      override def breed(a: Dog): List[Dog] = List()
    }
  }

  class Cat
  object Cat {
    implicit object CatAnimal extends Animal[Dog] {
      override def breed(a: Dog): List[Dog] = List()
    }
  }

  implicit class AnimalOps[A](animal: A) {
    def breed(implicit animalTypeClassInstance: Animal[A]): List[A] =
      animalTypeClassInstance.breed(animal)
  }

  val dog = new Dog
  dog.breed

  val cat = new Cat
//  cat.breed // wrong. missing right implicit




}
