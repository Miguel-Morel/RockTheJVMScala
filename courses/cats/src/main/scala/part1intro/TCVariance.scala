package part1intro

object TCVariance {

  import cats.Eq
  import cats.instances.int._ // Eq[Int] tc instance
  import cats.instances.option._ // construct a Eq[Option[Int]] tc instance
  import cats.syntax.eq._

  val aComparison = Option(2) === Option(3)
//  val anInvalidComparison = Some(2) === None // Eq[Some[Int]] implicit not found

  // variance
  class Animal
  class Cat extends Animal

  // covariant type. subtyping is propagated to the generic type
  class Cage[+T]
  val cage: Cage[Animal] = new Cage[Cat] // Cat <: Animal, so Cage[Cat] <: Cage[Animal]

  // contravariant type. subtyping is propagated backwards to generic type
  class Vet[-T]
  val vet: Vet[Cat] = new Vet[Animal] // Cat <: Animal, then Vet[Animal] <: Vet[Cat]

  // rule of thumb: if a generic type has a T = covariant. if it acts/operates on T = contravariant
  // variance will affect how tc instances are fetched

  // contravariant tc
  trait SoundMaker[-T]
  implicit object AnimalSoundMaker extends SoundMaker[Animal]
  def makeSound[T](implicit soundMaker: SoundMaker[T]): Unit = println("wow") // implementation not important
  makeSound[Animal] // ok - tc instance defined above
  makeSound[Cat] // ok - tc instance for Animal is also applicable to Cats

  // rule 1: contravariant tcs can use the superclass instances if nothing is available strictly for that type
  // has implications for subtypes
  implicit object OptionSoundMaker extends SoundMaker[Option[Int]]
  makeSound[Option[Int]]
  makeSound[Some[Int]]

  // covariant tc
  trait AnimalShow[+T] {
    def show: String
  }

  implicit object GeneralAnimalShow extends AnimalShow[Animal] {
    override def show: String = "animals everywhere"
  }

  implicit object CatShow extends AnimalShow[Cat] {
    override def show: String = "so many cats"
  }

  // rule 2: covariant tcs will always use the more specific tc instance for that type but may confuse the compiler if the general tc is also present
  def organizeShow[T](implicit event: AnimalShow[T]): String = event.show


  // rule 3: you can't have both benefits

  // Cats uses invariant type classes
  Option(2) === Option.empty[Int]

  def main(args: Array[String]): Unit = {
    println(organizeShow[Cat]) // ok - compiler injects CatsShow as implicit
//    println(organizeShow[Animal]) // will not compile - ambiguous values
  }



}
