package turbolift.abstraction.internals.interpreter
import mwords._
import turbolift.abstraction.!!
import turbolift.abstraction.effect.{EffectId, Signature}
import turbolift.abstraction.internals.handler.PrimitiveHandler


final class Interpreter[M[_], U](
  val theMonad: MonadPar[M],
  val handlerStacks: List[HandlerStack[M]],
  val failEffectId: EffectId,
) extends ((? !! U) ~> M) {
  override def apply[A](ua: A !! U): M[A] = loop(ua)

  def push[T[_[_], _], O[_], V](primitive: PrimitiveHandler[T, O]): Interpreter[T[M, ?], U with V] = {
    val newHead: HandlerStack[T[M, ?]] = HandlerStack.pushFirst(primitive)(this.theMonad)
    val newTail: List[HandlerStack[T[M, ?]]] = this.handlerStacks.map(_.pushNext(primitive))
    val newFailEffectId: EffectId = if (primitive.isFilterable) primitive.effectId else this.failEffectId
    new Interpreter[T[M, ?], U with V](newHead.outerMonad, newHead :: newTail, newFailEffectId)
  }

  def lookup(effectId: EffectId): Signature[M] = {
    def loop(i: Int): Signature[M] = {
      if (vmt(i) eq effectId)
        vmt(i+1).asInstanceOf[Signature[M]]
      else
      if (vmt(i+2) eq effectId)
        vmt(i+3).asInstanceOf[Signature[M]]
      else
      if (vmt(i+4) eq effectId)
        vmt(i+5).asInstanceOf[Signature[M]]
      else
        loop(i+2)
    }
    loop(0)
  }

  private val vmt: Array[AnyRef] = {
    val n = handlerStacks.size
    val arr = new Array[AnyRef]((n + 1) * 2)
    for ((hh, i) <- handlerStacks.iterator.zipWithIndex) {
      arr(i*2) = hh.effectId
      arr(i*2 + 1) = hh.decoder
    }
    arr(n*2) = null
    arr(n*2+1) = if (failEffectId == null) null else arr(arr.indexOf(failEffectId) + 1)
    arr
  }

  private def loop[A](ua: A !! U): M[A] = {
    import turbolift.abstraction.ComputationCases._
    implicit def M: MonadPar[M] = theMonad
    def castM[A1](ma: M[A1]) = ma.asInstanceOf[M[A]]
    def castS[M1[_], Z[P[_]] <: Signature[P]](f: Z[M1] => M1[A]) = f.asInstanceOf[Signature[M] => M[A]]
    ua match {
      case Pure(a) => theMonad.pure(a)
      case FlatMap(ux, k) => ux match {
        case Pure(x) => loop(k(x))
        case FlatMap(uy, j) => loop(FlatMap(uy, (y: Any) => FlatMap(j(y), k)))
        case _ => loop(ux).flatMap(x => loop(k(x)))
      }
      case ZipPar(uy, uz) => castM(loop(uy) *! loop(uz))
      case DispatchFO(id, op) => castS(op)(lookup(id))
      case DispatchHO(id, op) => castS(op(this))(lookup(id))
      case PushHandler(uy, ph) => castM(ph.prime(push(ph.primitive).loop(uy)))
    }
  }
}


object Interpreter {
  val pure: Interpreter[Trampoline, Any] = apply(TrampolineInstances.monad)
  val pureStackUnsafe: Interpreter[Identity, Any] = apply(MonadPar.identity)
  def apply[M[_]: MonadPar]: Interpreter[M, Any] = new Interpreter[M, Any](MonadPar[M], Nil, null)
}
