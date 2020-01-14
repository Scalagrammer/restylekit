package scg.restylekit.http.client.service

import cats.effect.Async
import akka.http.scaladsl.HttpExt
import scg.restylekit.common.syntax.data.|
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

trait HttpService {
  def apply[F[_] : Async](request : => HttpRequest) : F[HttpResponse]
}

class HttpServiceImpl(ext : HttpExt)(implicit context : ExecutionContext) extends HttpService {

  import ext.singleRequest
  import scg.restylekit.http.endpoint.module.HttpEndpointModuleImpl.tracingHeaders

  override def apply[F[_] : Async](request : => HttpRequest) : F[HttpResponse] = {

    def call(callback : (Throwable | HttpResponse) => Unit) : Unit = {
      singleRequest(request.copy(headers = request.headers ++ tracingHeaders()))
    }.onComplete { value =>
      callback(value.toEither)
    }

    Async[F].async(call)

  }
}
