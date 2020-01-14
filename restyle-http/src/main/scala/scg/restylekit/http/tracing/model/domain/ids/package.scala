package scg.restylekit.http.tracing.model.domain

import java.util.UUID

import scg.restylekit.http.routing.syntax.pathmatching.PlayJsonFormat
import scg.restylekit.model.wrapper.{UuidWrapper, WrapperCompanion}

package object ids {

  case class RequestId(value : UUID) extends AnyVal with UuidWrapper

  implicit object RequestId extends WrapperCompanion[RequestId] with PlayJsonFormat[RequestId]

  case class CorrelationId(value : UUID) extends AnyVal with UuidWrapper

  implicit object CorrelationId extends WrapperCompanion[CorrelationId] with PlayJsonFormat[CorrelationId]

}
