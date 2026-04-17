package lectures.part5ts

object Variance extends App {

  trait Animal
  class Dog extends Animal
  class Cat extends Animal
  class Crocodile extends Animal

  /*
    what is variance?
    "inheritance" - type substitution of generics
   */

  class Cage[T]
  // should a cage cat inherit from cage animal?
  // yes -> covariance
  class CCage[+T]
  val ccage: CCage[Animal] = new CCage[Cat]

  // no -> invariance
  class ICage[T]
//  val icage: CCage[Animal] = new ICage[Cat] <- can't inherit
//  val x: Int = "hello" <- same as saying

  // hell no -> opposite - contravariance
  class XCage[-T]
  val xCage: XCage[Cat] = new XCage[Animal]

  class InvariantCage[T](val animal: T) // invariant

  // covariant positions
  class CovariantCage[+T](val animal: T) // covariant position

//  class ContravariantCage[-T](val animal: T) <- doesn't work
  /*
    val catCage: XCage[Cat] = new XCage[Animal](new Crocodile)
   */

//  class CovariantVariableCage[+T](var animal: T) // types of vars are in contravariant position <- doesn't work
  /*
    val ccage: CCage[Animal] = new CCage[Cat](new Cat)
    ccage.animal = new Crocodile
   */

//  class ContravariantVariableCage[-T](var animal: T) // also in covariant position <- doesn't work
  /*
     val catCage: XCage[Cat] = new XCage[Animal](new Crocodile)
   */

  class InvariantVariableCage[T](var animal: T) // ok

//  trait AnotherCovariantCage[+T] { <- doesn't work
//    def addAnimal(animal: T) // contravariant position
//  }
  /*
    val ccage: CCage[Animal] = new CCage[Dog]
    ccage.add(new Cat)
   */

  class AnotherContravariantCage[-T] {
    def addAnimal(animal: T) = true
  }

  val acc: AnotherContravariantCage[Cat] = new AnotherContravariantCage[Animal]
  acc.addAnimal(new Cat)

  class Kitty extends Cat
  acc.addAnimal(new Kitty)

  class MyList[+A] {
    def add[B >: A](element: B): MyList[B] = new MyList[B] // widening the type
  }

  val emptyList = new MyList[Kitty]
  val animals = emptyList.add(new Kitty)
  val moreAnimals = animals.add(new Cat)
  val evenMoreAnimals = moreAnimals.add(new Dog)

  // method arguments are in contravariant position

  // return types
  class PetShop[-T] {
//    def get(isItAPuppy: Boolean): T // method return types are in covariant position
    /*
      val catShop = new PetShop[Animal] {
        def get(isItAPuppy: Boolean): Animal = new Cat
      }

      val dogShop: PetShop[Dog] = catShop
      dogShop.get(true) // evil cat
     */

    def get[S <: T](isItAPuppy: Boolean, defaultAnimal: S): S = defaultAnimal
  }

  val shop: PetShop[Dog] = new PetShop[Animal]
//  val evilCat = shop.get(true, new Cat) <- doesn't work

  class TerraNova extends Dog
  val bigFurry = shop.get(true, new TerraNova)

  /*
    big rule
      - method arguments are in contravariant position
      - return types are in covariant position
   */

  /*
    invariant, covariant, contravariant Parking[T](things List[T])
      1. Parking[T](things: List[T] {
            park(vehicle: T)
            impound(vehicles: List[T])
            checkVehicles(conditions: String): List[T]
          }

      2. what if we used someone else's API? Ilist[T]
      3. Parking = monad
          - flatMap
   */

  // 1.
  class Vehicle
  class Bike extends Vehicle
  class Car extends Vehicle
  class IList[T]

  class IParking[T](vehicle: List[T]) {
    def park(vehicle: T): IParking[T] = ???
    def impound(vehicle: List[T]): IParking[T] = ???
    def checkVehicles(conditions: String): List[T] = ???

    def flatMap[S](f: T => IParking[S]): IParking[S] = ???
  }

  class CParking[+T](vehicle: List[T]) {
    def park[S >: T](vehicle: S): CParking[S] = ???
    def impound[S >: T](vehicle: List[S]): CParking[S] = ???
    def checkVehicles(conditions: String): List[T] = ???

    def flatMap[S](f: T => CParking[S]): CParking[S] = ???
  }

  class XParking[-T](vehicle: List[T]) {
    def park(vehicle: T): XParking[T] = ???
    def impound(vehicles: List[T]): XParking[T] = ???
    def checkVehicles[S <: T](conditions: String): List[S] = ???

    def flatMap[R <: T, S](f: R => XParking[S]): XParking[S] = ???
//    def flatMap[R <: T, S](f: Function1[R, XParking[S]]): XParking[S] = ??? <- equivalent
  }

  /*
    rule of thumb
      - use covariance = collection of things (ie. vehicles)
      - use contravariance = group of actions
   */

  // 2.

  // invariant stays the same
  class IParking2[T](vehicle: List[T]) {
    def park(vehicle: T): IParking[T] = ???
    def impound(vehicle: List[T]): IParking[T] = ???
    def checkVehicles(conditions: String): List[T] = ???
  }

  class CParking2[+T](vehicle: IList[T]) {
    def park[S >: T](vehicle: S): CParking2[S] = ???
    def impound[S >: T](vehicle: IList[S]): CParking2[S] = ???
    def checkVehicles[S >: T](conditions: String): IList[S] = ???
  }

  class XParking2[-T](vehicle: IList[T]) {
    def park(vehicle: T): XParking2[T] = ???
    def impound[S <: T](vehicles: IList[S]): XParking2[S] = ???
    def checkVehicles[S <: T](conditions: String): IList[S] = ???
  }

  // 3.



}
