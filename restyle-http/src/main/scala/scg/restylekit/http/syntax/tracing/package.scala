package scg.restylekit.http.syntax

import cats.effect.Sync

import scg.restylekit.http.tracing.model.domain.ids.{CorrelationId, RequestId}
import scg.restylekit.http.endpoint.module.HttpEndpointModuleImpl.{getCorrId, getRqId}

import scala.language.higherKinds

package object tracing {

  def requestId[F[_] : Sync] : F[RequestId] = Sync[F].delay(RequestId(getRqId))

  def correlationId[F[_] : Sync] : F[CorrelationId] = Sync[F].delay(CorrelationId(getCorrId))

}
