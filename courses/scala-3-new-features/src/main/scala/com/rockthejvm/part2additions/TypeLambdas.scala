package com.rockthejvm.part2additions

object TypeLambdas {

  // higher-kinded types

  /*
    kinds = types of types
    - Int, String, Person = level-0 = value-level kind
    - List, Option, Future = level-1 kind ("generics")
    - Monad, Functor = level-2 kind ("generics of generics")
   */
  val aNumber: Int = 42
  val aList: List[Int] = List(1, 2, 3)
  //          ^^ type constructor: need to pass a type argument to turn into level-0 type
  // examples: List, Option, Map, Future, ...

  type MyList[A] = List[A]
  type MyListV2 = [A] =>> List[A] // type lambda, same as above
  val aList_v2: MyListV2[Int] = List(3, 4, 5) // ok

  type MyMap[K, V] = Map[K, V]
  type MyMapV2 = [K, V] =>> Map[K, V]

  class Functor[F[_]]
  //      ^^ type constructor: need to pass a level-1 type to obtain a level-0 type
  val functorOption = new Functor[Option]

  type MyFunctor[F[_]] = Functor[F]
  type MyFunctorV2 = [F[_]] =>> Functor[F] // same

  // application: generalizations or simplification of types
  type MySpecialMap = [A] =>> Map[String, A]
  val addressBook: MySpecialMap[String] = Map()

  // example: ZIO
  class ZIO[R, E, A]

  trait Monad[M[_]] {
    def flatMap[A, B](ma: M[A])(transformation: A => M[B]): M[B]
  }

  class ZIOMonad[R, E] extends Monad[[A] =>> ZIO[R, E, A]]:
    override def flatMap[A, B](ma: ZIO[R, E, A])(transformation: A => ZIO[R, E, B]): ZIO[R, E, B] = new ZIO


  // exercise - implement a "monad" data type for Either
  val aLeft: Either[String, Int] = Left("scala")
  val aRight: Either[String, Int] = Right(42)

  class EitherMonad[E] extends Monad[[A] =>> Either[E, A]]:
    override def flatMap[A, B](ma: Either[E, A])(transformation: A => Either[E, B]): Either[E, B] = ma match
      case Left(e) => Left(e)
      case Right(v) => transformation(v)



}

