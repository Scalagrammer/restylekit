package scg.restylekit.common.utils

trait Convertible[A, B] {
  def apply(from : A) : B
}

object Convertible {
  def apply[A, B](f : A => B) : Convertible[A, B] = f(_)
}
