package scg.restylekit.http.routing

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaType.WithFixedCharset
import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.StatusCodes.InternalServerError
import akka.http.scaladsl.model.{ContentTypeRange, MessageEntity}
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, FromRequestUnmarshaller, Unmarshaller}
import akka.util.ByteString

import scg.restylekit.http.routing.alerting.{ApiError, ErrorAlert}
import scg.restylekit.http.routing.response.model.entity.ResponseEntity

import com.typesafe.scalalogging.Logger

import play.api.libs.json.Json.{parse, prettyPrint}
import play.api.libs.json.{JsValue, Reads, Writes}

import scg.restylekit.http.routing.response.model.ResponseCode
import scg.restylekit.http.routing.response.schema.ResponseStatus.Failed

import scala.collection.immutable.Seq
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

package object marshalling {

  import scg.restylekit.common.syntax.impure._

  implicit def marshaller[A : Writes] : ToEntityMarshaller[A] = {
    jsonStringMarshaller
      .compose(prettyPrint)
      .compose(Writes.of[A].writes)
  }

  implicit def unmarshaller[A : Reads] : FromEntityUnmarshaller[A] = {

    def read(json : JsValue) = {
      Reads.of[A].reads(json)
    }.recoverTotal { cause =>
      throw ApiError(ErrorAlert(code = invalidEntityResponseCode, message = "Invalid json entity")) <<* {
        logger.error("An error has occurred during json read operation", cause)
      }
    }

    jsonStringUnmarshaller.map(data => read(parse(data)))

  }

  def unmarshallerContentTypes : Seq[ContentTypeRange] = mediaTypes.map(ContentTypeRange(_))

  def mediaTypes : Seq[WithFixedCharset] = List(`application/json`)

  def bodyAs[T : FromRequestUnmarshaller] : Directive1[T] = {

    import akka.http.scaladsl.server.directives.BasicDirectives._
    import akka.http.scaladsl.server.directives.RouteDirectives.complete
    import akka.http.scaladsl.server.directives.FutureDirectives.onComplete

    import scg.restylekit.http.routing.response.model.entity.ResponseEntityJson._

    extractRequestContext.flatMap[Tuple1[T]] { ctx =>

      import ctx.{executionContext, materializer}

      onComplete(implicitly[FromRequestUnmarshaller[T]].apply(ctx.request))
        .flatMap {
          case Success(value) =>
            provide(value)
          case Failure(ApiError(alerts)) =>
            complete(ResponseEntity[JsValue](alerts = alerts))
          case Failure(NonFatal(cause)) =>
            complete(ResponseEntity[JsValue](code = InternalServerError, status = Failed)) <<*
              logger.error("An exception has occurred during request entity unmarshalling", cause) // dirty
        }
    }
  }

  private[this] def jsonStringUnmarshaller : FromEntityUnmarshaller[String] = {
    Unmarshaller.byteStringUnmarshaller.forContentTypes(unmarshallerContentTypes : _ *)
  }.mapWithCharset {
    case (ByteString.empty, _) =>
      throw ApiError(ErrorAlert(code = invalidEntityResponseCode, message = "Message entity must not be empty")) <<* {
        logger.error("Unexpected empty message for unmarshalling process")
      }
    case (data, charset) =>
      data.decodeString(charset.nioCharset.name)
  }

  private[this] lazy val jsonStringMarshaller : Marshaller[String, MessageEntity] = {
    Marshaller.oneOf(mediaTypes : _ *)(Marshaller.stringMarshaller)
  }

  private[this] lazy val invalidEntityResponseCode : Option[ResponseCode] = Some(ResponseCode("invalid_entity"))

  private[this] val logger = Logger(getClass)

}
