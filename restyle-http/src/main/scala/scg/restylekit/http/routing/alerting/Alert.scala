package scg.restylekit.http.routing.alerting

import java.time.ZonedDateTime

import enumeratum.EnumEntry.Lowercase
import enumeratum.{Enum, EnumEntry, PlayJsonEnum}
import scg.restylekit.common.temporal.model.Temporal

import scala.collection.immutable.IndexedSeq
import scg.restylekit.http.routing.response.model.ResponseCode

import scg.restylekit.http.routing.alerting.Level.{Debug, Fatal, Info, Trace, Warning, Error}

sealed trait Alert {
  def level     : Level
  def message   : String
  def element   : Option[String]
  def timestamp : ZonedDateTime
  def code      : Option[ResponseCode]
}

case class InfoAlert(message : String, code : Option[ResponseCode] = None, element : Option[String] = None, timestamp : ZonedDateTime = Temporal()) extends Alert {
  final override def level : Level = Info
}

case class ErrorAlert(message : String, code : Option[ResponseCode] = None, element : Option[String] = None, timestamp : ZonedDateTime = Temporal()) extends Alert {
  final override def level : Level = Level.Error
}

case class DebugAlert(message : String, code : Option[ResponseCode] = None, element : Option[String] = None, timestamp : ZonedDateTime = Temporal()) extends Alert {
  final override def level : Level = Debug
}

case class FatalAlert(message : String, code : Option[ResponseCode] = None, element : Option[String] = None, timestamp : ZonedDateTime = Temporal()) extends Alert {
  final override def level : Level = Level.Fatal
}

case class TraceAlert(message : String, code : Option[ResponseCode] = None, element : Option[String] = None, timestamp : ZonedDateTime = Temporal()) extends Alert {
  final override def level : Level = Level.Trace
}

case class WarnAlert(message : String, code : Option[ResponseCode] = None, element : Option[String] = None, timestamp : ZonedDateTime = Temporal()) extends Alert {
  final override def level : Level = Level.Warning
}

object Alert {

  import play.api.libs.json._

  implicit val jsonFormat : OFormat[Alert] = OFormat(reads, writes)

  private[this] lazy val writes : OWrites[Alert] = (a : Alert) => {
    (__ \ "level").write[Level].writes(a.level) ++
      (__ \ "code").writeNullable[ResponseCode].writes(a.code) ++
       (__ \ "message").write[String].writes(a.message) ++
        (__ \ "timestamp").write[ZonedDateTime].writes(a.timestamp) ++
         (__ \ "element").writeNullable[String].writes(a.element)
  }

  private[this] lazy val reads : Reads[Alert] = for {
    code <- (__ \ "code").readNullable[ResponseCode]
    level <- (__ \ "level").read[Level]
   message <- (__ \ "message").read[String]
    element <- (__ \ "element").readNullable[String]
   timestamp <- (__ \ "timestamp").read[ZonedDateTime]
  } yield level match {
    case Info    =>
      InfoAlert(message, code, element, timestamp)
    case Error   =>
      ErrorAlert(message, code, element, timestamp)
    case Debug   =>
      DebugAlert(message, code, element, timestamp)
    case Warning =>
      WarnAlert(message, code, element, timestamp)
    case Fatal   =>
      FatalAlert(message, code, element, timestamp)
    case Trace   =>
      TraceAlert(message, code, element, timestamp)
  }
}

sealed trait Level extends EnumEntry with Lowercase

object Level extends Enum[Level] with PlayJsonEnum[Level] {

  case object Info extends Level

  case object Error extends Level

  case object Debug extends Level

  case object Fatal extends Level

  case object Trace extends Level

  case object Warning extends Level

  def defaultLevel : Level = Info

  override val values : IndexedSeq[Level] = findValues

}