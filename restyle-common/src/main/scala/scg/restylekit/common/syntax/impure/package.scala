package scg.restylekit.common.syntax

package object impure {
  implicit class SideEffectSyntax[A](private val a : A) extends AnyVal {
    def =<*[R](f : A => R) : A = f.andThen(_ => a)(a)

    def <<*[R](f : => R) : A = (a =<* (_ => f))
  }
}
