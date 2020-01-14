package scg.restylekit.http.materializer.module

import akka.stream.{ActorMaterializer, Materializer}
import scg.restylekit.common.actors.module.ActorsModule

trait MatModule {
  implicit def materializer : Materializer
}

trait MatModuleImpl extends MatModule {

  this : ActorsModule =>

  override implicit lazy val materializer : Materializer = ActorMaterializer()
}
