package scg.restylekit.common.uuid.service

import java.util.UUID

import cats.effect.Sync

import scg.restylekit.model.wrapper.{UuidWrapper, WrapperCompanion}

import scala.language.higherKinds

trait UuidService[F[_]] extends (() => F[UUID]) {

  override def apply() : F[UUID]

  def wrapNext[W <: UuidWrapper : WrapperCompanion] : F[W]

}

object UuidService {
  def apply[F[_] : Sync](supply : => UUID) : UuidService[F] = new UuidService[F] { self =>

    import cats.syntax.functor._

    override def apply() : F[UUID] = Sync[F].delay(supply)

    override def wrapNext[W <: UuidWrapper : WrapperCompanion] : F[W] = self().map(implicitly[WrapperCompanion[W]].apply)

  }
}