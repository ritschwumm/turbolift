package turbolift.std_effects
import turbolift.abstraction.!!
import turbolift.abstraction.effect.{Effect, Signature}
import turbolift.std_handlers.DefaultChoiceHandler


trait ChoiceSig[U] extends Signature[U] {
  def empty[A]: A !! U
  def plus[A](lhs: A !! U, rhs: => A !! U): A !! U
  def each[A](as: Iterable[A]): A !! U
}


trait Choice extends Effect[ChoiceSig] {
  final val empty: Nothing !! this.type = encodeFO(_.empty)
  final def plus[A, U](lhs: A !! U, rhs: => A !! U): A !! U with this.type = encodeHO[U](_.plus(lhs, rhs))
  final def each[A](as: Iterable[A]): A !! this.type = encodeFO(_.each(as))

  //@#@ rename to apply?
  final def fromEach[A](as: A*): A !! this.type = each(as.toVector)

  val handler = DefaultChoiceHandler(this)
}
