package part2abstractMath

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

object MonadTransformers {

  def sumAllOptions(values: List[Option[Int]]): Int = ???

  import cats.data.OptionT
  import cats.instances.list._ // fetch an implicit Monad[List]


  // Option transformer
  val listOfNumberOptions: OptionT[List, Int] = OptionT(List(Option(1), Option(2)))
  val listOfCharOptions: OptionT[List, Char] = OptionT(List(Option('a'), Option('b'), Option.empty[Char]))
  val listOfTuples: OptionT[List, (Int, Char)] = for {
    char <- listOfCharOptions
    number <- listOfNumberOptions
  } yield (number, char)

  // Either transformer
  import cats.data.EitherT
  val listOfEithers: EitherT[List, String, Int] = EitherT(List(Left("something wrong"), Right(43), Right(2)))
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(8))
//  val futureOfEither: EitherT[Future, String, Int] = EitherT(Future[Either[String,Int]](Right(45)))
  val futureOfEither: EitherT[Future, String, Int] = EitherT.right(Future(45)) // wrap over Future(Right(45))

  /*
    todo exercise
    We have a multi-machine cluster for your busienss which will receive a traffic surge following media appeareance.
    We measure bandwidth in units.
    We want to allocate two of our servers to cope with the traffic spike.
    We know the current capacity for each server and we konw we'll hold the traffic if the sum of bandwidths is > 250.
   */

  val bandwidths = Map(
    "server1.rockthejvm.com" -> 50,
    "server2.rockthejvm.com" -> 300,
    "server3.rockthejvm.com" -> 300
  )

  type AsyncResponse[T] = EitherT[Future, String, T] // wrapper over Future[Either[String, T]]

  def getBandwidth(server: String): AsyncResponse[Int] = bandwidths.get(server) match {
//    case None => EitherT(Future[Either[String,Int]](Left(s"server $server unreachable")))
    case None => EitherT.left(Future(s"server $server unreachable"))
    case Some(b) => EitherT.right(Future(b))
  }

  // todo 1
  // call getBandwidth twice, and combine results
  def canWithstandSurge(s1: String, s2: String): AsyncResponse[Boolean] = for {
    band1 <- getBandwidth(s1)
    band2 <- getBandwidth(s2)
  } yield band1 + band2 > 250
  // Future[Either[String, Boolean]]

  // todo 2
  // call canWithstandSurge + transform
  def generateTrafficSpikeReport(s1: String, s2: String): AsyncResponse[String] =
    canWithstandSurge(s1, s2).transform {
      case Left(reason) => Left(s"servers $s1 and $s2 cannot cope with incoming spike: $reason")
      case Right(false) => Left(s"servers $s1 and $s2 cannot cope with incoming spike: not enough total bandwidth")
      case Right(true) => Right(s"servers $s1 and $s2 can cope with incoming spike no problem")
    }
  // ^^^^^^^^^^^^^^^^^^                      ^^^^^^^^^^^^^^^^^^^^^
  // Future[Either[String, Boolean]] ----- Future[Either[String, String]]


  def main(args: Array[String]): Unit = {
    println(listOfTuples.value)
    val resultFuture = generateTrafficSpikeReport("server2.rockthejvm.com", "server3.rockthejvm.com").value
    resultFuture.foreach(println)
  }

}
