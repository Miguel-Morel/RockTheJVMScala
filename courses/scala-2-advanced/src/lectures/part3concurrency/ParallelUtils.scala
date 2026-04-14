package lectures.part3concurrency

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.atomic.AtomicReference
import scala.collection.parallel.{ForkJoinTaskSupport, Task, TaskSupport}
import scala.collection.parallel.immutable.ParVector

object ParallelUtils extends App {

  //1. parallel collections
  val parList = List(1,2,3).par

  val aParVector = ParVector[Int](1,2,3)

  /*
    Seq
    Vector
    Array
    Map - Hash, Trie
    Set - Hash, Trie

    parallelizing collections usually increases PERF
   */

  def measure[T](operation: => T): Long = {
    val time = System.currentTimeMillis()
    operation
    System.currentTimeMillis() - time
  }

  val list = (1 to 10000).toList

  val serialTime = measure {
    list.map(_ + 1)
  }
  println("serial time:  " + serialTime)

  val parallelTime = measure {
    list.par.map(_ + 1)
  }


  println("parallel time: " + parallelTime)

  /*
    small instances of collections are faster in serial than parallel.
    parallel collections operate on the map-reduce model
      - split elements into chunks - Splitter
      - operation on chunks on separate threads
      - recombine - combiner (reduce step)

    they have: map, flatMap, filter, foreach, reduce, fold
   */

  // careful with fold and reduce. parallelized collections with non-associative operators - may yield unexpected results
  println(List(1,2,3).reduce(_ - _))
  println(List(1,2,3).par.reduce(_ - _))

  // synchronization
  var sum = 0
  List(1,2,3).par.foreach(sum += _)
  println(sum) // race conditions !!!

  // configuring
  aParVector.tasksupport = new ForkJoinTaskSupport(new ForkJoinPool(2))

  /*
    alternatives
      - ThreadPoolTaskSupport - deprecated
      - ExecutionContextTaskSupport(EC)
   */

  // creating own new task support
  aParVector.tasksupport = new TaskSupport {
    // scheduels a thread to run in parallel
    override def execute[R, Tp](fjtask: Task[R, Tp]): () => R = ???

    // same as above but blocking - wait for the thread to join
    override def executeAndWaitResult[R, Tp](task: Task[R, Tp]): R = ???

    // number of cpu cores it should join
    override def parallelismLevel: Int = ???

    // actual thread manager
    override val environment: AnyRef = ???
  }


  //2. atomic operations and references. atomic: operation that cannot be intercepted by another thread
  val atomic = new AtomicReference[Int](2)

  val currentValue = atomic.get() // thread-safe read
  atomic.set(4) // thread-safe write

  atomic.getAndSet(5) // thread-safe combo

  // if the value is 38, then set to 56 - thread-safe
  // reference equality
  atomic.compareAndSet(38, 56)

  atomic.updateAndGet(_ + 1) // thread-safe function run

  atomic.getAndUpdate(_ + 1) // reverse of above

  atomic.accumulateAndGet(12, _ + _) // thread-safe accumulation

  atomic.getAndAccumulate(12, _ + _) // reverse of above







}
