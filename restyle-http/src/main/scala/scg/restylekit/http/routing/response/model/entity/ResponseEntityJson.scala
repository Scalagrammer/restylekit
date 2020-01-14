package scg.restylekit.http.routing.response.model.entity

import java.time.ZonedDateTime, java.util.Locale

import akka.http.scaladsl.model.StatusCode
import play.api.libs.json.{Format, Reads, Writes}

import scg.restylekit.http.routing.alerting.Alert
import scg.restylekit.http.tracing.model.domain.ids.RequestId
import scg.restylekit.http.routing.response.schema.ResponseStatus

trait ResponseEntityJson {

  import play.api.libs.json._
  import ResponseEntityJson._

  implicit def jsonFormat[Data : Format] : EntityFormat[Data] = Format(reads[Data], writes[Data])

  implicit def reads[Data : Reads] : EntityReads[Data] = for {
    code   <- (__ \ "code").read[StatusCode]
    locale  <- (__ \ "locale").read[Locale]
    result   <- (__ \ "result").readNullable[Data]
    timestamp <- (__ \ "timestamp").read[ZonedDateTime]
    status     <- (__ \ "status").read[ResponseStatus]
    requestId   <- (__ \ "rqId").read[RequestId]
    alerts       <- (__ \ "alerts").readNullable[Seq[Alert]]
  } yield {
    ResponseEntity(locale, requestId, alerts.getOrElse(NoAlerts), code, result, timestamp, status)
  }

  implicit def writes[Data : Writes] : EntityWrites[Data] = {
    case ResponseEntity(locale, rqId, alerts, code, result, timestamp, status) =>
      (__ \ "code").write[StatusCode].writes(code) ++
       (__ \ "locale").write[Locale].writes(locale) ++
        (__ \ "result").writeNullable[Data].writes(result) ++
         (__ \ "timestamp").write[ZonedDateTime].writes(timestamp) ++
          (__ \ "status").write[ResponseStatus].writes(status) ++
           (__ \ "rqId").write[RequestId].writes(rqId) ++
            (__ \ "alerts").writeNullable[Seq[Alert]].writes {
              if (alerts.isEmpty) None else Some(alerts)
            }
  }
}

object ResponseEntityJson extends ResponseEntityJson {

  type Entity[Data]       = ResponseEntity[Data]
  type EntityReads[Data]  = Reads[Entity[Data]]
  type EntityWrites[Data] = Writes[Entity[Data]]
  type EntityFormat[Data] = Format[Entity[Data]]

  private[entity] implicit val statusCodeFormat : Format[StatusCode] = {
    Format(Reads.of[Int].map(StatusCode.int2StatusCode(_ : Int)), code => Writes.of[Int].writes(code.intValue()))
  }

  private[response] val NoAlerts = Seq.empty[Alert]

}