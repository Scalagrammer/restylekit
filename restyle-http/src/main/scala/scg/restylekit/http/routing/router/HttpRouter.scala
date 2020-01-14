package scg.restylekit.http.routing.router

import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.model.{HttpHeader, HttpResponse}
import akka.http.scaladsl.server
import akka.http.scaladsl.server.{Directives, Route}

import scala.collection.immutable.Seq

trait HttpRouter extends Directives {

  def route : Route

  import HttpRouter._

  def cors(origin : Route) : Route = {
    respondWithHeaders(corsHeaders)(preflightRequestHandler ~ origin)
  }
}

object HttpRouter {

  import server.Directives._

  import scala.concurrent.duration._

  private[router] lazy val preflightRequestHandler : Route = options {
    complete {
      HttpResponse(OK).withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, PATCH, GET, DELETE))
    }
  }

  // TODO: Access-Control-Max-Age and Access-Control-Allow-Headers - make it configurable

  private[router] lazy val corsHeaders : Seq[HttpHeader] = {
    `Access-Control-Allow-Origin`.*                                                   ::
    `Access-Control-Allow-Credentials`(true)                                           ::
    `Access-Control-Max-Age`(1.day.toMillis)                                            ::
    `Access-Control-Allow-Headers`("Authorization", "Content-Type", "X-Requested-With")  :: Nil
  }
}