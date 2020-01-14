package scg.restylekit.http.routing.response.module

import scg.restylekit.http.routing.response.schema.ResponseSchema.Eventual

import scala.language.higherKinds

trait EventualF[F[_]] {
  implicit def eventual : Eventual[F]
}


