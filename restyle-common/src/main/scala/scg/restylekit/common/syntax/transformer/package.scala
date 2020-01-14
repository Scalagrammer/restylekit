package scg.restylekit.common.syntax

import cats.{Functor, Monad}
import cats.data.{EitherT, Ior, IorT, OptionT}

import scg.restylekit.common.syntax.data.{^, |}

import scala.language.{higherKinds, implicitConversions}

package object transformer {

  implicit class OptionTransformer[F[_], A](private val fa : F[Option[A]]) extends AnyVal {
    def optionT : OptionT[F, A] = OptionT(fa)

    def orF[B >: A](fb : => F[B])(implicit m : Monad[F]) : F[B] = optionT.getOrElseF(fb)

    def or[B >: A](b : => B)(implicit f : Functor[F]) : F[B] = optionT.getOrElse(b)
  }

  implicit class EitherTransformer[F[_], A, B](private val fa : F[A | B]) extends AnyVal {
    def eitherT : EitherT[F, A, B] = EitherT(fa)

    def orF[BB >: B](fbb : => F[BB])(implicit m : Monad[F]) : F[BB] = eitherT.getOrElseF(fbb)

    def or[BB >: B](bb : => BB)(implicit f : Functor[F]) : F[BB] = eitherT.getOrElse(bb)
  }

  implicit class IorTransformer[F[_], A, B](private val fa : F[A ^ B]) extends AnyVal {
    def iorT : IorT[F, A, B] = IorT(fa)
  }

  implicit def impliedF[F[_], A](transformer : OptionT[F, A]) : F[Option[A]] = transformer.value

  implicit def impliedF[F[_], L, R](transformer : EitherT[F, L, R]) : F[Either[L, R]] = transformer.value

  implicit def impliedF[F[_], L, R](transformer : IorT[F, L, R]) : F[Ior[L, R]] = transformer.value

}
