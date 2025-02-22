package turbolift.internals.effect
import cats.Functor
import turbolift.{Signature, Effect, Handler}
import turbolift.internals.interpreter.{Interpreter => IC}


trait CanInterpret extends HasSignature:
  enclosing =>
  private[turbolift] def signatures: Array[Signature]

  /** Alias for [[Handler]], specialized to eliminate this effect. */
  final type ThisHandler[F[+_], N] = Handler[F, ThisEffect, N]

  /** Defines type aliases for [[Handler]], specialized to eliminate this effect. */
  object ThisHandler:
    /** Alias for [[Handler.Id]], specialized to eliminate this effect. */
    type Id[N] = Handler.Id[ThisEffect, N]

    /** Alias for [[Handler.Const]], specialized to eliminate this effect. */
    type Const[T, N] = Handler.Const[T, ThisEffect, N]

    /** Alias for [[Handler.Free]], specialized to eliminate this effect. */
    type Free[F[+_]] = Handler.Free[F[+_], ThisEffect]

    /** Alias for [[Handler.FreeId]], specialized to eliminate this effect. */
    type FreeId = Handler.FreeId[ThisEffect]

    /** Alias for [[Handler.FreeConst]], specialized to eliminate this effect. */
    type FreeConst[T] = Handler.FreeConst[T, ThisEffect]

  sealed trait ThisInterpreter extends IC.Unsealed:
    final override type ThisEffect = enclosing.ThisEffect
    private[turbolift] final override val signatures: Array[Signature] = enclosing.signatures

  /** Base class for user-defined [[turbolift.internals.interpreter.Interpreter.Stateless Stateless Interpreter]] for this effect. */
  abstract class Stateless[F[+_]: Functor] extends IC.Stateless[F] with ThisInterpreter { thiz: ThisSignature => }

  /** Base class for user-defined [[turbolift.internals.interpreter.Interpreter.Stateful Stateful Interpreter]] for this effect. */
  abstract class Stateful[S, F[+_]: Functor] extends IC.Stateful[S, F] with ThisInterpreter { thiz: ThisSignature => }

  /** Base class for user-defined [[turbolift.internals.interpreter.Interpreter.Proxy Proxy Interpreter]] for this effect. */
  abstract class Proxy[Fx] extends IC.Proxy[Fx] with ThisInterpreter { thiz: ThisSignature => }
