package scg.restylekit.http.routing.response.model.entity

import java.time.ZonedDateTime
import java.util.Locale

import ResponseEntityJson.NoAlerts
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.OK
import scg.restylekit.http.routing.alerting.Alert
import scg.restylekit.common.temporal.model.Temporal
import scg.restylekit.http.tracing.model.domain.ids.RequestId
import scg.restylekit.http.endpoint.module.HttpEndpointModuleImpl
import scg.restylekit.http.routing.response.schema.ResponseStatus

case class ResponseEntity[Data](locale    : Locale,
                                rqId      : RequestId,
                                alerts    : Seq[Alert],
                                code      : StatusCode,
                                result    : Option[Data],
                                timestamp : ZonedDateTime,
                                status    : ResponseStatus)

object ResponseEntity {

  import HttpEndpointModuleImpl.getRqId

  def apply[Data](locale : Locale = Locale.getDefault, rqId : RequestId = RequestId(getRqId), alerts : Seq[Alert] = NoAlerts, code : StatusCode = OK, result : Option[Data] = None, timestamp : ZonedDateTime = Temporal(), status : ResponseStatus = ResponseStatus.Ok) : ResponseEntity[Data] = {
    new ResponseEntity(locale, rqId, alerts, code, result, timestamp, status)
  }

}