package part4typeclassses

import cats.{Applicative, Apply}

object WeakerMonads {

//  trait MyMonad[M[_]] {
//    def pure[A](value: A): M[A]
//    def flatMap[A, B](ma: M[A])(f: A => M[B]): M[B]
//    def map[A, B](ma: M[A])(f: A => B): M[B] =
//      flatMap(ma)(x => pure(f(x)))
//  }

  trait MyFlatMap[M[_]] extends Apply[M]{
    def flatMap[A, B](ma: M[A])(f: A => M[B]): M[B]

    // todo: implement ap
    // hint: Apply extends Functor
    def ap[A, B](wf: M[A => B])(wa: M[A]): M[B] = {
      flatMap(wa)(a => map(wf)(f => f(a)))
    //         |  |        /   \     \/
    //         |  |    M[A=>B] A=>B  B
    //         |  |    \_____  _____/
    //      M[A]  A =>      M[B]
    }
  }
  trait MyMonad[M[_]] extends Applicative[M] with MyFlatMap[M] {
    override def map[A, B](ma: M[A])(f: A => B): M[B] =
      flatMap(ma)(x => pure(f(x)))

  }

  import cats.FlatMap
  import cats.syntax.flatMap._ // flatMap extension method
  import cats.syntax.functor._ // map extension method

//  def getPairs[M[_]: FlatMap](numbers: M[Int], chars: M[Char]): M[(Int, Char)] = for {
  def getPairs[M[_]: FlatMap, A, B](numbers: M[A], chars: M[B]): M[(A, B)] = for {
    n <- numbers
    c <- chars
  } yield (n, c)



  def main(args: Array[String]): Unit = {

  }

}
