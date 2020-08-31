package turbolift.std_effects
import cats.Semigroup
import turbolift.abstraction.!!
import turbolift.abstraction.effect.{Effect, Signature}
import turbolift.abstraction.typeclass.Accum
import turbolift.std_handlers.DefaultValidationHandler


trait ValidationSig[U, E] extends Signature[U] {
  def invalid[A](e: E): A !! U
  def validate[A](scope: A !! U)(recover: E => A !! U): A !! U
}


trait Validation[E] extends Effect[ValidationSig[?, E]] {
  def invalid(e: E): Nothing !! this.type = encodeFO(_.invalid(e))
  def invalid[X](x: X)(implicit ev: Accum[X, E]): Nothing !! this.type = invalid(ev.one(x))
  def validate[A, U](scope: A !! U)(recover: E => A !! U): A !! U with this.type = encodeHO[U](_.validate(scope)(recover))

  def from[A](x: Either[E, A]): A !! this.type = x match {
    case Right(a) => pure(a)
    case Left(e) => invalid(e)
  }

  def handler(implicit E: Semigroup[E]) = DefaultValidationHandler[E, this.type](this)
}
