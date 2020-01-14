package scg.restylekit.http.ext.module

import akka.http.scaladsl.{Http, HttpExt}
import scg.restylekit.common.actors.module.ActorsModule

trait ExtModule {
  def ext : HttpExt
}

trait ExtModuleImpl extends ExtModule {

  this : ActorsModule =>

  override lazy val ext : HttpExt = Http()

}
