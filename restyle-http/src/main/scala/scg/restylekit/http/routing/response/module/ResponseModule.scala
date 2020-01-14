package scg.restylekit.http.routing.response.module

import scg.restylekit.http.routing.response.schema.ResponseSchema

import scala.language.higherKinds

trait ResponseModule[F[_]] {
  def responseSchema : ResponseSchema[F]
}

trait ResponseModuleImpl[F[_]] {

  this : EventualF[F] =>

  lazy val responseSchema : ResponseSchema[F] = ResponseSchema[F]

}
