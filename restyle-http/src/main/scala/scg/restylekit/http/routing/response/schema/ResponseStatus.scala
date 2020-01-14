package scg.restylekit.http.routing.response.schema

import enumeratum.EnumEntry.Lowercase
import enumeratum._

import scala.collection.immutable.IndexedSeq

sealed trait ResponseStatus extends EnumEntry with Lowercase

object ResponseStatus
  extends Enum[ResponseStatus]
    with PlayLowercaseJsonEnum[ResponseStatus] {

  case object Ok      extends ResponseStatus
  case object Failed  extends ResponseStatus
  case object Doing   extends ResponseStatus
  case object Warning extends ResponseStatus

  override val values : IndexedSeq[ResponseStatus] = findValues

}