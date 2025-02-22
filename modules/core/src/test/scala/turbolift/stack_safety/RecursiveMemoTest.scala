package turbolift.stack_safety
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers._
import turbolift.!!
import turbolift.std_effects.Reader
import turbolift.extra_effects.{AcyclicMemoizer, CyclicMemoizer}


class RecursiveMemoTest extends AnyFunSpec with CanStackOverflow:
  describe("AcyclicMemoizer effect should be stack safe") {
    case object FxMemo extends AcyclicMemoizer[Int, Int]

    def fun = FxMemo.fix { recur => n =>
      if n > 0 then
        for
          x <- recur(n - 1)
        yield x + 1
      else
        !!.pure(1)
    }

    val cache = mustNotStackOverflow {
      fun(TooBigForStack - 1)
      .&&!(FxMemo.get)
      .runWith(FxMemo.handler)
    }

    cache.size shouldEqual TooBigForStack
  }


  describe("CyclicMemoizer effect should be stack safe") {
    case class Node(
      label: Int,
      prev: () => Node,
      next: () => Node,
    )

    case object FxMemo extends CyclicMemoizer[Int, Node]

    def fun(m: Int) = FxMemo.fix { recur => n =>
      for
        prev <- recur(0.max(n - 1))
        next <- recur(m.min(n + 1))
        node = Node(label = n, prev = prev, next = next) 
      yield node
    }

    val cache = mustNotStackOverflow {
      fun(TooBigForStack - 1)(0)
      .&&!(FxMemo.get)
      .runWith(FxMemo.handler)
    }

    cache.size shouldEqual TooBigForStack
  }

