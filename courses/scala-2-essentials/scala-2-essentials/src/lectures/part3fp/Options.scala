package lectures.part3fp

import scala.util.Random

object Options extends App {

  val myFirstOption: Option[Int] = Some(4)
  val noOption: Option[Int] = None

  println(myFirstOption)

  // WORKing with unsafe APIs

  // options were invented to deal with unsafe APIs
  def unsafeMethod(): String = null
//  val result = Some(null) // WRONG
  val result = Option(unsafeMethod()) // Some or None

  println(result)

  // useful in chained methods
  def backupMethod(): String = "A valid result"
  val chainedResult = Option(unsafeMethod()).orElse(Option(backupMethod()))

  // DESIGNing unsafe APIs
  // make sure to make your methods return Option(Something), instead of returning Null

  def betterUnsafeMethod(): Option[String] = None
  def betterBackupMethod(): Option[String] = Some("A valid result")

  val betterChainedResult = betterUnsafeMethod() orElse betterBackupMethod()

  // functions on Options
  println(myFirstOption.isEmpty)
  println(myFirstOption.get) // UNSAFE - DO NOT USE THIS

  // map, flatMap, filter
  println(myFirstOption.map(_ * 2))
  println(myFirstOption.filter(_ > 10))
  println(myFirstOption.flatMap(x => Option(x * 10)))

  // for comprehensions

  /*
    Exercise

   */
  val config: Map[String, String] = Map(
    // fetched from elsewhere (no certainty there are values)
    "host" -> "176.45.36.1",
    "port" -> "80"
  )

  class Connection {
    def connect = "Connected" // connect to some server
  }

  object Connection {
    val random = new Random(System.nanoTime())

    def apply(host: String, port: String): Option[Connection] =
      if (random.nextBoolean()) Some(new Connection)
      else None
  }

  // try to establish a connection. if able to, print the connect method
  val host = config.get("host")
  val port = config.get("port")
  /*
    if (h != null)
      if (p != null)
        return Connection.apply(h,p)
    return null
   */
  val connection = host.flatMap(h => port.flatMap(p => Connection(h, p)))
  /*
    if (c != null)
      return c.connect
    return null
   */
  val connectionStatus = connection.map(_.connect)
  /*
    if (connectionStatus == null) println(None
    else println(Some(connectionStatus.get))
   */
  println(connectionStatus)
  /*
    if (status != null)
      println(status)
   */
  connectionStatus.foreach(println)


  // alt solution
  // chained calls

  config.get("host")
    .flatMap(host => config.get("port")
      .flatMap(port => Connection(host,port))
      .map(_.connect))
    .foreach(println)

  // for comprehensions
  val forConnectionStatus = for {
    host <- config.get("host")
    port <- config.get("port")
    connection <- Connection(host,port)
  } yield connection.connect

  forConnectionStatus.foreach(println)

}
