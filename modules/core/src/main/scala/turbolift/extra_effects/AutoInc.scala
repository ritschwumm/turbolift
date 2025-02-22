package turbolift.extra_effects
import turbolift.{!!, Effect, Signature}
import turbolift.extra_effects.default_handlers.AutoIncHandler


trait AutoIncSig extends Signature:
  def next: Int !@! ThisEffect


trait AutoInc extends Effect[AutoIncSig] with AutoIncSig:
  def next: Int !! this.type = perform(_.next)

  /** Default handler for this effect. */
  def handler: ThisHandler.Free[(_, Int)] = handler(0)
  
  /** Predefined handler for this effect. */
  def handler(initial: Int): ThisHandler.Free[(_, Int)] = AutoIncHandler.apply(this, initial)
