package lectures.part5ts

import lectures.part5ts.StructuralTypes.AdvancedCloseable

object StructuralTypes extends App {

  // structural types

  type JavaCloseable = java.io.Closeable

  class HipsterCloseable {
    def close(): Unit = println("closing")
    def closeSilently(): Unit = println("not making a sound")
  }

//  def closeQuietly(closeable: JavaCloseable OR HipsterCloseable) // ??

  //structural type
  type UnifiedCloseable = {
    def close(): Unit
  }

  def closeQuietly(unifiedCloseable: UnifiedCloseable): Unit = unifiedCloseable.close()

  closeQuietly(new JavaCloseable {
    override def close(): Unit = println("shh")
  })

  closeQuietly(new HipsterCloseable)

  // type refinements
  type AdvancedCloseable = JavaCloseable {
    def closeSilently(): Unit
  }

  class AdvancedJavaCloseable extends JavaCloseable {
    override def close(): Unit = println("java closes")
    def closeSilently(): Unit = println("java closes silently")
  }

  def closeShh(advCloseable: AdvancedCloseable): Unit = advCloseable.closeSilently()

  closeShh(new AdvancedJavaCloseable)
//  closeShh(new HipsterCloseable) <- doesn't work

  // using structural types as standalone types
  def altClose(closeable: {def close(): Unit}): Unit = closeable.close()

  // type-checking => duck-typing
  type SoundMaker = {
    def makeSound(): Unit
  }

  class Dog {
    def makeSound(): Unit = println("bark")
  }

  class Car {
    def makeSound(): Unit = println("vroom")
  }

  // static duck-typing
  val dog: SoundMaker = new Dog
  val car: SoundMaker = new Car

  // CAVEAT: based on reflection

  // FYI: reflection calls have a big impact on performance. only use when absolutely necessary.

  /*
    Exercise

    1. if f compatible with CBL and with a human? yes, it's compatible with both
   */

  trait CBL[+T] {
    def head: T
    def tail: CBL[T]
  }

  class Human {
    def head: Brain = new Brain
  }

  class Brain {
    override def toString: String = "brainz"
  }

  def f[T](somethingWithAHead: {def head: T}): Unit = println(somethingWithAHead.head)

  case object CBNil extends CBL[Nothing] {
    def head: Nothing = ???
    def tail: CBL[Nothing] = ???
  }

  case class CBCons[T](override val head: T, override val tail: CBL[T]) extends CBL[T]

  f(CBCons(2, CBNil))
  f(new Human) // ?? T = Brain

  // 2. is HeadEqualizer compatible with a CBL and a human?
  object HeadEqualizer {
    type Headable[T] = {
      def head: T
    }
    def ===[T](a: Headable[T], b: Headable[T]): Boolean = a.head == b.head
  }

  val brainzList = CBCons(new Brain, CBNil)

  val stringsList = CBCons("brainz", CBNil)
  HeadEqualizer.===(brainzList, new Human)
  // problem
  HeadEqualizer.===(new Human, stringsList) // not type-safe
}

