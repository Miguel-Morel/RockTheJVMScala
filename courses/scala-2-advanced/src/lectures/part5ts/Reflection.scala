package lectures.part5ts

object Reflection extends App {

  // how do I instantiate a class or invoke a method by calling just its name dynamically at runtime? => reflection

  // reflection macros + quasinotes => metaprogramming

  case class Person(name: String) {
    def sayMyName(): Unit = println(s"hi, my name is $name")
  }

  // 0 - import
  import scala.reflect.runtime.{universe => ru}

  // 1 - instantiate a mirror
  val m = ru.runtimeMirror(getClass.getClassLoader)

  // 2 - create a class object = "description"
  val clazz = m.staticClass("lectures.part5ts.Reflection.Person") // by name

  // 3 - create a reflected mirror = "can do things"
  val cm = m.reflectClass(clazz)

  // 4 - get the constructor
  val constructor = clazz.primaryConstructor.asMethod

  // 5 - reflect the constructor
  val constructionMirror = cm.reflectConstructor(constructor)

  // 6 - invoke the constructor
  val instance = constructionMirror.apply("John")

  println(instance)

  // I have an instance
  val p = Person("Mary") // from the wire as a serialized object

  // method name copmuted from somewhere else
  val methodName = "sayMyName"

  // 1 - mirror
  // 2 - reflect the instance
  val reflected = m.reflect(p)

  // 3 - method symbol
  val methodSymbol = ru.typeOf[Person].decl(ru.TermName(methodName)).asMethod

  // 4 - reflect the method = can do things
  val method = reflected.reflectMethod(methodSymbol)

  // 5 - invoke the method
  method.apply()



}
