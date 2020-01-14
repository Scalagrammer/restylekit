package scg.restylekit.http

import akka.stream.Materializer
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal

import cats.effect.Async

import com.github.ghik.silencer.silent
import com.typesafe.scalalogging.Logger
import play.api.libs.json.Reads

import scala.language.higherKinds
import scala.util.control.NonFatal
import scala.concurrent.ExecutionContext

package object response {

  import scg.restylekit.common.syntax.impure._
  import scg.restylekit.http.routing.marshalling._

  implicit class RichHttpResponse(private val response : HttpResponse) extends AnyVal {
    @silent def entityAs[E : Reads, F[_] : Async](implicit mat : Materializer, context : ExecutionContext) : F[Option[E]] = {
      Async[F].async { callback =>
        Unmarshal(response.entity)
          .to[E]
          .map(Some(_))
          .recover { case NonFatal(cause) =>
            None <<* logger.error("An error has occurred during entity unmarshalling", cause)
          }.andThen { case _ =>
            response.discardEntityBytes()
          }.onComplete { value =>
            callback(value.toEither)
          }
      }
    }
  }

  private[this] lazy val logger = Logger[RichHttpResponse]

}
