package scg.restylekit.http.config

import scg.restylekit.common.config.model.PrefixedKeys

import scala.concurrent.duration.FiniteDuration

case class HttpEndpointConfig(bindingPort      :            Int,
                              endpointName     :         String,
                              bindingInterface :         String,
                              unbindTimeout    : FiniteDuration,
                              contextPath      : Option[String])

object HttpEndpointConfig extends PrefixedKeys("http-endpoint") {

  import net.ceedubs.ficus.Ficus._
  import net.ceedubs.ficus.readers.ValueReader

  implicit val httpApiEndpointConfigReader : ValueReader[HttpEndpointConfig] = ValueReader.relative { cfg =>
    HttpEndpointConfig(cfg.as[Int]("binding.port"), cfg.as[String]("name"), cfg.as[String]("binding.interface"), cfg.as[FiniteDuration]("binding.unbind-timeout"), cfg.getAs[String]("binding.context-path"))
  }
}