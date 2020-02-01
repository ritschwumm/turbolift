package turbolift.abstraction.internals.handler
import mwords.{~>}
import cats.{Functor, Id}


trait Lifting[Outer[_], Inner[_], Stash[_]] {
  type Unlift = Outer ~> Lambda[X => Inner[Stash[X]]]
  def stashFunctor: Functor[Stash]
  def lift[A](la: Inner[A]): Outer[A]
  def withUnlift[A](ff: Unlift => Inner[Stash[A]]): Outer[A]
}


object Lifting {
  def compose[Outer[_], Middle[_], Inner[_], StashO[_], StashI[_]](outer: Lifting[Outer, Middle, StashO], inner: Lifting[Middle, Inner, StashI]): Lifting[Outer, Inner, Lambda[X => StashI[StashO[X]]]] = {
    type Stash[A] = StashI[StashO[A]]

    new Lifting[Outer, Inner, Stash] {
      def stashFunctor: Functor[Stash] = inner.stashFunctor.compose(outer.stashFunctor)
  
      def lift[A](la: Inner[A]): Outer[A] = outer.lift(inner.lift(la))

      def withUnlift[A](ff: Unlift => Inner[Stash[A]]): Outer[A] =
        outer.withUnlift { runOuter =>
          inner.withUnlift { runInner =>
            ff(new Unlift {
              def apply[A](ua: Outer[A]): Inner[Stash[A]] = runInner(runOuter(ua))
            })
          }
        }
    }
  }


  def identity[M[_]]: Lifting[M, M, Id] =
    new Lifting[M, M, Id] {
      def stashFunctor: Functor[Id] = Functor[Id]

      def lift[A](ma: M[A]): M[A] = ma

      def withUnlift[A](ff: Unlift => M[A]): M[A] =
        ff(new Unlift {
          def apply[A](ma: M[A]): M[A] = ma
        })
    }
}
