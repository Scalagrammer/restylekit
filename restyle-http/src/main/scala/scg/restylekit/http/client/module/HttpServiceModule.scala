package scg.restylekit.http.client.module

import scg.restylekit.common.execution.module.ExecutionModule
import scg.restylekit.http.client.service.{HttpService, HttpServiceImpl}
import scg.restylekit.http.ext.module.ExtModule

trait HttpServiceModule {
  def httpService : HttpService
}

trait HttpServiceModuleImpl extends HttpServiceModule {

  this : ExtModule
    with ExecutionModule =>

  import com.softwaremill.macwire.wire

  override lazy val httpService : HttpService = wire[HttpServiceImpl]

}


