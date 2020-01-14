package scg.restylekit.http.routing.response.schema


import akka.http.scaladsl.model.{HttpHeader, StatusCode}
import akka.http.scaladsl.server.Directives.{complete, onComplete, respondWithHeaders}
import akka.http.scaladsl.server.Route
import cats.arrow.FunctionK
import com.typesafe.scalalogging.Logger
import scg.restylekit.common.syntax.data.->
import scg.restylekit.http.routing.response.model.entity.ResponseEntityJson.EntityWrites
import scg.restylekit.http.routing.response.model.entity.ResponseEntityJson
import scg.restylekit.http.routing.response.schema.ResponseSchema.Eventual
import play.api.libs.json.{JsValue, Writes}
import scg.restylekit.http.routing.alerting.ApiError

import scala.concurrent.Future
import scala.language.{higherKinds, implicitConversions}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

class ResponseSchema[F[_] : Eventual] extends ResponseEntityJson {

  import Eventual.swapF
  import scg.restylekit.common.syntax.impure._
  import scg.restylekit.http.routing.marshalling._
  import scg.restylekit.http.routing.response.model.entity.ResponseEntity
  import scg.restylekit.http.endpoint.module.HttpEndpointModuleImpl.tracingHeaders

  implicit def entityRouteMagnet[Data : Writes](stack : => F[ResponseEntity[Data]]) : Route = {
    onComplete(stack) { trying =>
      respondWithHeaders(tracingHeaders()) {
        complete {
          trying match {
            case Failure(cause) => matchCause(cause)
            case Success(responseEntity) =>
              responseEntity
          }
        }
      }
    }
  }

  implicit def dataRouteMagnet[Data : EntityWrites](stack : => F[Data]) : Route = {
    onComplete(stack) { trying =>
      respondWithHeaders(tracingHeaders()) {
        complete {
          trying match {
            case Failure(cause) => matchCause(cause)
            case Success(data) =>
              ResponseEntity(result = Some(data))
          }
        }
      }
    }
  }

  implicit def httpHeaderRouteMagnet(stack : => F[HttpHeader]) : Route = {
    onComplete(stack) {
      case Failure(cause) =>
        respondWithHeaders(tracingHeaders())(complete(matchCause(cause)))
      case Success(header) =>
        respondWithHeaders(header +: tracingHeaders())(complete(ResponseEntity[JsValue]()))
    }
  }

  implicit def unitRouteMagnet(stack : => F[Unit]) : Route = {
    onComplete(stack) { trying =>
      respondWithHeaders(tracingHeaders()) {
        complete {
          trying match {
            case Failure(cause) => matchCause(cause)
            case Success(_) =>
              ResponseEntity[JsValue]()
          }
        }
      }
    }
  }

  implicit def statusAndCodeRouteMagnet(stack : => F[StatusCode -> ResponseStatus]) : Route = {
    onComplete(stack) { trying =>
      respondWithHeaders(tracingHeaders()) {
        complete {
          trying match {
            case Failure(cause) => matchCause(cause)
            case Success(code -> status) =>
              ResponseEntity[JsValue](status = status, code = code)
          }
        }
      }
    }
  }

  private[this] def matchCause(cause : Throwable) : ResponseEntity[JsValue] = cause match {
    case ApiError(alerts) =>
      ResponseEntity[JsValue](alerts = alerts)
    case _ =>
      ResponseEntity[JsValue]() <<* logger.error("Unhandled exception has occurred during request processing", cause)
  }

  private[this] val logger = Logger[ResponseSchema[F]]

}

object ResponseSchema {

  def apply[F[_] : Eventual] : ResponseSchema[F] = new ResponseSchema[F]

  trait Eventual[F[_]] extends FunctionK[F, Future] {
    override def apply[A](fa : F[A]) : Future[A]
  }

  object Eventual {

    def apply[F[_]](k : FunctionK[F, Future]) : Eventual[F] = new Eventual[F] {
      override def apply[A](fa : F[A]) : Future[A] = k(fa)
    }

    implicit def swapF[F[_], A](fa : => F[A])(implicit e : Eventual[F]) : Future[A] = {
      try e(fa) catch {
        case NonFatal(cause) => Future.failed(cause)
      }
    }
  }
}