package scg.restylekit.common.config.module

import com.typesafe.config.{Config, ConfigFactory}

trait ConfigModule {
  def cfg : Config
}

object ConfigModule {
  lazy val restyleVersion : String = {
    ConfigFactory
      .load("restyle-build.conf")
      .getString("restyle-build.version")
  }
}

trait ConfigModuleImpl extends ConfigModule {
  override lazy val cfg : Config = ConfigFactory.load().resolve()
}
