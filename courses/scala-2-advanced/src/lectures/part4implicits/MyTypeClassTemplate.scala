package lectures.part4implicits

// HTMLSerializer ->TYPE CLASS: specifies a set of operations (ie. serialize) that can be applied to a given type
// type class template
trait MyTypeClassTemplate[T] {
  def action(value: T): String
}

object MyTypeClassTemplate {
  def apply[T](implicit instance: MyTypeClassTemplate[T]) = instance
}