package lectures.part3fp

object TuplesAndMaps extends App {

  // tuples = finite ordered "lists"
  val aTuple = (2, "hello scala") // Tuple2[Int, String] = (Int, String)

  println(aTuple._1) // 2
  println(aTuple.copy(_2 = "goodbye java")) // (2, goodbye java)
  println(aTuple.swap) // ("hello scala, 2)

  // Map = associate keys -> values
  val aMap: Map[String, Int] = Map()

  // a -> b is sugar for (a,b)
  val aPhoneBook = Map(("Jim", 555), "Daniel" -> 789).withDefaultValue(-1)
  println(aPhoneBook)

  // map ops
  println(aPhoneBook.contains("Jim"))
  println(aPhoneBook("Mary"))

  // add a pairing
  val newPairing = "Mary" -> 678
  val aNewPhoneBook = aPhoneBook + newPairing
  println(aNewPhoneBook)

  // functionals on maps
  // map, flatMap, filter

  println(aPhoneBook.map(pair => pair._1.toLowerCase -> pair._2))

  // filterKeys
  println(aPhoneBook.filterKeys(x => x.startsWith("J")))

  // mapValues
  println(aPhoneBook.mapValues(number => "0245-" + number))

  // conversions to other collections
  println(aPhoneBook.toList)
  println(List(("Daniel", 555)).toMap)

  val names = List("Bob", "James", "Angela", "Mary", "Daniel", "Jim")
  println(names.groupBy(name => name.charAt((0))))


}
