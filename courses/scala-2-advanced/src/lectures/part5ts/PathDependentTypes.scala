package lectures.part5ts

object PathDependentTypes extends App {

  class Outer {
    class Inner
    object InnerObject
    type InnerType

    def print(i: Inner) = println(i)
    def printGeneral(i: Outer#Inner) = println(i)
  }

  def aMethod: Int = {
    class HelperClass
    type HelperType = String
    2
  }

  // defined per-instance
  val o = new Outer
  val inner = new o.Inner // o.inner is a type

  val oo = new Outer
//  val otherInner: oo.Inner = new o.Inner <- doesn't compile, different types

  o.print(inner)
//  oo.print(inner) <- doesn't compile, different types

  // path-dependent types

  // all Inner types have a common supertype: Outer#Inner
  o.printGeneral(inner)
  oo.printGeneral(inner)

  /*
    Exercise

    db keyed by Int or String, but maybe others
   */

  /*
    use path-dependent types
    abstract type members and/or type aliases
   */

  trait ItemLike {
    type Key
  }

  trait Item[K] extends ItemLike {
    type Key = K
  }
  trait IntItem extends Item[Int]
  trait StringItem extends Item[String]

  def get[ItemType <: ItemLike](key: ItemType#Key): ItemType = ???

  get[IntItem](42) // ok
  get[StringItem]("home") // ok
//  get[IntItem]("scala") // not ok

}
