package turbolift.std_effects
import turbolift.{!!, Effect, Signature}
import turbolift.std_effects.default_handlers.ReaderHandler


trait ReaderSig[R] extends Signature:
  def ask: R !@! ThisEffect
  def asks[A](f: R => A): A !@! ThisEffect
  def localPut[A, U <: ThisEffect](r: R)(body: A !! U): A !@! U
  def localModify[A, U <: ThisEffect](f: R => R)(body: A !! U): A !@! U


trait Reader[R] extends Effect[ReaderSig[R]] with ReaderSig[R]:
  final override val ask: R !! this.type = operate(_.ask)
  final override def asks[A](f: R => A): A !! this.type = operate(_.asks(f))
  final override def localPut[A, U <: this.type](r: R)(body: A !! U): A !! U = operate(_.localPut(r)(body))
  final override def localModify[A, U <: this.type](f: R => R)(body: A !! U): A !! U = operate(_.localModify(f)(body))

  def handler(initial: R): ThisHandler.FreeId = ReaderHandler(this, initial)
