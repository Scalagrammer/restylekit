package scg.restylekit.common.execution.module

import scg.restylekit.common.actors.module.ActorsModule

import scala.concurrent.ExecutionContext

trait ExecutionModule {
  implicit def context : ExecutionContext
}

trait ExecutionModuleImpl extends ExecutionModule {

  this : ActorsModule =>

  override implicit lazy val context : ExecutionContext = actors.dispatcher

}
