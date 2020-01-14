package scg.restylekit.http.routing

import cats.ApplicativeError

import scala.language.higherKinds

package object alerting {
  implicit class AlertOps[A <: Alert](private val a : A) extends AnyVal {
    def raise[F[_]](implicit e : ApplicativeError[F, Throwable]) : F[Nothing] = e.raiseError(ApiError(a))
  }
}
