package scg.restylekit.http.endpoint.module

import java.lang.String.valueOf
import java.util.UUID
import java.util.UUID.randomUUID
import java.util.regex.Pattern

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{HttpEntity, HttpHeader}
import akka.http.scaladsl.server.directives.RouteDirectives.reject
import akka.http.scaladsl.server.{MalformedHeaderRejection, Route, RouteResult}
import akka.http.scaladsl.{Http, settings}
import cats.effect.IO
import scg.restylekit.common.actors.module.ActorsModule
import scg.restylekit.common.execution.module.ExecutionModule
import scg.restylekit.common.lifecycle.Hook
import scg.restylekit.common.syntax.data.->
import scg.restylekit.http.endpoint.module.HttpEndpointModule.HttpBinding
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger

import scala.concurrent.{Future, Promise}
import scala.util.{Failure, Success}
import akka.http.scaladsl.server.RouteConcatenation._
import akka.http.scaladsl.server.RouteResult.{Complete, Rejected}
import cats.effect.IO.{fromEither, raiseError}
import org.slf4j.MDC
import scg.restylekit.http.ext.module.ExtModule
import scg.restylekit.http.config.HttpEndpointConfig
import scg.restylekit.http.routing.router.HttpRouter
import scg.restylekit.http.materializer.module.MatModule

import scala.collection.immutable.Seq

trait HttpEndpointModule {
  def Endpoint(routees : Set[HttpRouter])(cfg : Config) : Hook
}

object HttpEndpointModule {
  object HttpBinding {
    def unapply(binding : ServerBinding) : Option[String -> Int] = {
      Some(binding.localAddress.getHostName -> binding.localAddress.getPort)
    }
  }
}

trait HttpEndpointModuleImpl extends HttpEndpointModule {

  this : MatModule
    with ExtModule
    with ActorsModule
    with ExecutionModule =>

  import cats.syntax.apply._
  import cats.syntax.functor._

  import HttpEndpointModuleImpl.traceableRoute

  import net.ceedubs.ficus.Ficus._
  import HttpEndpointModuleImpl.foldConcat
  import scg.restylekit.common.logging.markers._
  import scg.restylekit.http.config.HttpEndpointConfig.prefix

  override def Endpoint(routees : Set[HttpRouter])(cfg : Config) : Hook = new Hook {

    val httpApiCfg : HttpEndpointConfig = cfg.as[HttpEndpointConfig](prefix)

    val boundHandle : Promise[ServerBinding] = Promise[ServerBinding]

    import httpApiCfg.{bindingInterface, bindingPort, endpointName, unbindTimeout}

    override def startup() : IO[Unit] = IO.fromFuture {
      IO(ext.bindAndHandle(traceableRoute(foldConcat(routees)), bindingInterface, bindingPort, ext.defaultServerHttpContext, settings.ServerSettings(implicitly[ActorSystem])))
    }.runAsync {
      case Right(b@HttpBinding(host, port)) if boundHandle.trySuccess(b) =>
        logger.infoF[IO](logs("host" -> host, "port" -> port, "endpoint" -> endpointName), "Endpoint started")
      case Left(cause) if boundHandle.tryFailure(cause) =>
        logger.errorF[IO]("Endpoint binding failed", cause) *> raiseError(cause)
      case any =>
        logger.errorF[IO]("Bound handle is broken") *> fromEither(any).as()
    }.toIO

    override def shutdown() : IO[Unit] = IO.fromFuture(IO(unbind())).as()

    private[this] def unbind() : Future[Http.HttpTerminated] = {
      boundHandle.future.flatMap(_.terminate(unbindTimeout))
    }.andThen {
      case Failure(cause) =>
        logger.error("Shutdown fail", cause)
      case Success(_) =>
        logger.info(logs("endpoint" -> endpointName), "Shutdown gracefully")
    }
  }

  private[this] val logger = Logger[HttpEndpointModuleImpl]

}

private[http] object HttpEndpointModuleImpl {

  import keys._

  import scala.collection.breakOut

  import MDC.{clear => clearContext}

  import scg.restylekit.common.ops.StringExt._
  import scg.restylekit.common.syntax.impure._
  import scg.restylekit.common.logging.markers._

  def tracingHeaders() : Seq[HttpHeader] = {

    def requestIdHeader(value : String) : HttpHeader = RawHeader(`X-Request-Id`, value)

    def correlationIdHeader(value : String) : HttpHeader = RawHeader(`X-Correlation-Id`, value)

    (find(CorrelationId).map(correlationIdHeader) ++ find(RequestId).map(requestIdHeader)).to(breakOut)

  }

  def getRqId : UUID = find(RequestId).getOrElse(nextUuidAsString()).toUuid

  def getCorrId : UUID = find(CorrelationId).getOrElse(nextUuidAsString()).toUuid

  private[module] def traceableRoute(origin : Route) : Route = { requestContext =>

    def validateRequest() : Either[RouteResult, (Option[String], String, String, Seq[HttpHeader])] = {

      import requestContext.request
      import requestContext.request.headers

      def extractPath() : String = (requestContext.unmatchedPath.toString())

      def extractMethod() : String = (request.method.value)

      headers.find(_.is(`x-correlation-id`)).map(_.value()) match {
        case Some(value) if isUuid(value) =>
          Right(Some(value), extractPath(), extractMethod(), headers)
        case Some(_) =>
          Left(Rejected(Seq(MalformedHeaderRejection(`X-Correlation-Id`, "Invalid format - expected UUID"))))
        case _ =>
          Right(None, extractPath(), extractMethod(), headers)
      }
    }

    def registerRequest(correlationId : String, path : String, method : String, headers : Seq[HttpHeader]) : Unit = {

      def set(key : String)(any : Any) = (any.toString) =<* (value => MDC.put(key, value))

      set(CorrelationId)(correlationId)

      set(RequestId)(nextUuidAsString())

      logger.info(logs("method" -> method, "path" -> path, "headers" -> headers.mkString(", ")), "Incoming request")

    }

    import requestContext.executionContext

    validateRequest() match {
      case Left(routeResult) =>
        Future.successful(routeResult)
      case Right((maybeCorrId, path, method, headers)) =>
        origin(requestContext <<* registerRequest(maybeCorrId.getOrElse(nextUuidAsString()), path, method, headers))
          .andThen {
            case Failure(cause) =>
              logger.error("An error has occurred during request processing", cause)
            case Success(Rejected(rejections)) =>
              logger.info(logs("rejections" -> rejections.mkString(",")), "Request rejected")
            case Success(Complete(response)) => import response.{entity, status}
              val markers = {
                logs("status" -> status.intValue()).withMarkers("headers" -> response.headers)
              }.withMaybeMarker {
                "entity" -> {
                  entity match {
                    case strict : HttpEntity.Strict if !(entity.isKnownEmpty()) => Some(strict.data.utf8String)
                    case _ => None
                  }
                }
              }
              logger.info(markers, "Request processed successfully")
          }.andThen {
          case _ => clearContext() // MDC.clear()
        }
    }
  }

  private[module] def nextUuidAsString() : String = valueOf(randomUUID())

  private[module] def foldConcat(rs : Set[HttpRouter]) : Route = rs.map(_.route).fold(reject)(_ ~ _)

  private[this] def find(key : String) = Option(MDC.get(key))

  private[this] val isUuid : (String => Boolean) = {
    Pattern
      .compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")
      .asPredicate()
      .test(_)
  }

  private[this] val logger = Logger[this.type]

  private[this] object keys {
    val RequestId          = "requestId"
    val CorrelationId      = "correlationId"

    val `X-Request-Id`     = "X-Request-Id"
    val `X-Correlation-Id` = "X-Correlation-Id"
    val `x-correlation-id` = "x-correlation-id"
  }

}
