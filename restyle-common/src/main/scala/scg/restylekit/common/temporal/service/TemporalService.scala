package scg.restylekit.common.temporal.service

import java.time.Clock

import cats.effect.Sync
import scg.restylekit.common.temporal.model.Temporal

import scala.language.higherKinds

trait TemporalService[F[_]] extends (() => F[Temporal]) {
  override def apply() : F[Temporal]
}

object TemporalService {
  def apply[F[_] : Sync](clock : Clock) : TemporalService[F] = { () => Sync[F].delay(Temporal(clock)) }
}
