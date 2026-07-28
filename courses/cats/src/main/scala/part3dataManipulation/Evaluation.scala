package part3dataManipulation

object Evaluation {

  /*
    Cats makes the distinction between
    - evaluating an expression eagerly
    - evaluating lazily and every time you request it
    - evaluating lazily and keeping the value (memoizing)
   */

  import cats.Eval
  val instantEval = Eval.now {
    println("computing now")
    12345
  }

  val redoEval = Eval.always {
    println("computing again")
    4234
  }

  val delayedVal = Eval.later {
    println("computing later")
    53248
  }

  val composedEvaluation = instantEval.flatMap(value1 => delayedVal.map(value2 => value1 + value2))

  val anotherComposedEvaluation = for {
    value1 <- instantEval
    value2 <- delayedVal
  } yield value1 + value2

  // TODO 1: predict the output
  val evalEx1 = for {
    a <- delayedVal
    b <- redoEval
    c <- instantEval
    d <- redoEval
  } yield a + b + c + d
  // now, later, again, again, sum, again, again, sum

  // "remember" a computed value
  val dontRecompute = redoEval.memoize

  val tutorial = Eval
    .always { println("step 1..."); "put the guitar on your lap"}
    .map { step1 => println("step 2..."); s"$step1 then put your left hand on the neck"}
    .memoize // remember the value up to this point
    .map {steps12 => println("step 3, more complicated"); s"$steps12 then with the right hand strike the strings"}

  // todo 2: implement this method such that defer(Eval.now) does NOT run the side effects
  def defer[T](eval: => Eval[T]): Eval[T] =
    Eval.later(()).flatMap(_ => eval)

  // todo 3: rewrite the method with Evals
  def reverseList[T](list: List[T]): List[T] =
    if(list.isEmpty) list
    else reverseList(list.tail) :+ list.head

  def reverseEval[T](list: List[T]): Eval[List[T]] =
    if (list.isEmpty) Eval.now(list)
    else defer(reverseEval(list.tail).map(_ :+ list.head)) // Eval.defer works too


  def main(args: Array[String]): Unit = {

    println(defer(Eval.now {
      println("now")
      42
    }).value)

    println(reverseEval((1 to 100000).toList).value)

//    println(tutorial.value)
//    println(tutorial.value)
//
//    println(dontRecompute.value)
//    println(dontRecompute.value)

    // ex1
//    println(evalEx1.value)
//    println(evalEx1.value)

//    println(composedEvaluation.value)
//    println(delayedVal.value)
//    println(redoEval.value)
//    println(instantEval.value)

  }

}
