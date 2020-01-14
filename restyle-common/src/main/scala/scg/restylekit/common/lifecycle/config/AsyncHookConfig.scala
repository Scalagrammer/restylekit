package scg.restylekit.common.lifecycle.config

import net.ceedubs.ficus.readers.ValueReader
import scg.restylekit.common.config.model.PrefixedKeys

import scala.concurrent.duration.FiniteDuration

case class AsyncHookConfig(startupTimeout : FiniteDuration, shutdownTimeout : FiniteDuration)

object AsyncHookConfig extends PrefixedKeys("app-lifecycle") {

  import net.ceedubs.ficus.Ficus._

  implicit val asyncHookConfigReader : ValueReader[AsyncHookConfig] = ValueReader.relative { cfg =>
    AsyncHookConfig(cfg.as[FiniteDuration]("startup-timeout"), cfg.as[FiniteDuration]("shutdown-timeout"))
  }
}
