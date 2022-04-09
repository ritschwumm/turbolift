package turbolift.std_effects
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers._
import turbolift.!!
import turbolift.std_effects.{Choice, Except}


class ChoiceTest extends AnyFunSpec with CanLaunchTheMissiles:
  private class Picker(round: Boolean):
    def apply[T](a: => T)(b: => T): T = if round then a else b
    def name = apply("one")("many")
    def handler[Fx <: Choice](fx: Fx): fx.ThisHandler.Free[Vector] =
      apply(fx.handlers.one.toVector)(fx.handlers.many)

  private object Picker:
    def foreach(f: Picker => Unit): Unit =
      f(Picker(false))
      //@#@TODO
      // for round <- List(true, false) do
      //   f(Picker(round))


  describe("Basic ops") {
    case object Fx extends Choice
    for picker <- Picker do
      val handler = picker.handler(Fx)
      describe("With handler = " + picker.name) {
        it("choose") {
          Fx.choose(List(1, 2))
          .runWith(handler) shouldEqual picker(Vector(1))(Vector(1, 2))
        }

        it("empty") {
          (Fx.empty &&! !!.pure(1))
          .runWith(handler) shouldEqual Vector()
        }

        it("plus") {
          Fx.plus(!!.pure(1), !!.pure(2))
          .runWith(handler) shouldEqual picker(Vector(1))(Vector(1, 2))
        }

        it("plus fail") {
          (!!.pure(1) ||! !!.fail ||! !!.pure(2))
          .runWith(handler) shouldEqual picker(Vector(1))(Vector(1, 2))
        }
      }
  }


  describe("Combined ops") {
    case object Fx extends Choice
    for picker <- Picker do
      val handler = picker.handler(Fx)
      describe("With handler = " + picker.name) {
        it("Nested choose") {
          (for
            n <- Fx.choose(1 to 2)
            c <- Fx.choose('a' to 'b')
          yield s"$n$c")
          .runWith(handler) shouldEqual picker(Vector("1a"))(Vector("1a", "1b", "2a", "2b"))
        }

        it("Nested choose with guard") {
          (for
            n <- Fx.choose(1 to 2)
            if n % 2 == 0
            c <- Fx.choose('a' to 'b')
          yield s"$n$c")
          .runWith(handler) shouldEqual picker(Vector("2a"))(Vector("2a", "2b"))
        }

        it("Nested plus") {
          (for
            n <- !!.pure(1) ||! !!.pure(2)
            c <- !!.pure('a') ||! !!.pure('b')
          yield s"$n$c")
          .runWith(handler) shouldEqual picker(Vector("1a"))(Vector("1a", "1b", "2a", "2b"))
        }

        it("Nested plus with guard") {
          (for
            n <- !!.pure(1) ||! !!.pure(2)
            _ <- if n % 2 == 0 then Fx.choose(n to n) else Fx.empty
            // if n % 2 == 0
            c <- !!.pure('a') ||! !!.pure('b')
          yield s"$n$c")
          .runWith(handler) shouldEqual picker(Vector("2a"))(Vector("2a", "2b"))
        }


        describe("Choice & Except") {
          case object FxE extends Except[Int]
          val hE = FxE.handler
          val hC = picker.handler(Fx)

          val comp = !!.pure(1) ||! FxE.raise(2)

          it("Writer before Choice") {
            comp.runWith(hE >>>! hC) shouldEqual picker(Vector(Right(1)))((Vector(Right(1), Left(2))))
          }

          it("Choice before Writer") {
            comp.runWith(hC >>>! hE) shouldEqual picker(Right(Vector(1)))(Left(2))
          }
        }
      }
  }


/*


  describe("fail") {
    case object Fx extends Choice

    val missile1 = Missile()
    val missile2 = Missile()

    (for
      i <- !!.pure(123)
      _ <- Fx.fail *! missile1.launch_!
      _ <- missile2.launch_!
    yield i)
    .runWith(Fx.handlers.one) shouldEqual None
    
    missile1.mustHaveLaunchedOnce
    missile2.mustNotHaveLaunched
  }
*/