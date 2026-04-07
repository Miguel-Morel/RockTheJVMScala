package lectures.part2oop

object AbstractDataTypes extends App {

  //abstract
  abstract class Animal {
    val creatureType: String = "wild"
    def eat: Unit
  }

  class Dog extends Animal {
    override val creatureType: String = "canine"
    def eat: Unit = println("crunch") //override doesn't need to be specified here, as the compiler infers the abstract method being overriden as there is no prior implementation of it.
  }

  //traits = scala's ultimate abstract data types. can be extended alongside classes
  trait Carnivore {
    def eat(animal: Animal): Unit
    val preferredMeal: String = "fresh meat"
  }

  trait ColdBlooded

  class Crocodile extends Animal with Carnivore with ColdBlooded {
    override val creatureType: String = "croc"
    def eat: Unit = println("nomnom")
    def eat(animal: Animal): Unit = println(s"I'm a croc and I'm eating ${animal.creatureType}")
  }

  val dog = new Dog
  val croc = new Crocodile
  croc.eat(dog)

  //traits vs abstract classes
  //abstract classes can have abstract AND non-abstract types, and so can traits.
  // 1 - traits don't have constructor parameters (until Scala 3)
  // 2 - multiple traits may be inherited by the same class
  // 3 - traits = behavior; abstract class = thing (ie. trait = carnivore; abstract class = animal)

}
