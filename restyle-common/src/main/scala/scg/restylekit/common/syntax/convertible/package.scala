package scg.restylekit.common.syntax

import scg.restylekit.common.utils.Convertible

package object convertible {
  implicit class ConvertibleSyntax[A](private val a : A) extends AnyVal {
    def to[B](implicit converter : Convertible[A, B]) : B = converter(a)
  }
}
