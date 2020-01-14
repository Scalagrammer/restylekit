package scg.restylekit.common.lifecycle

import cats.effect.{ContextShift, IO, Timer}
import com.typesafe.config.Config

import scg.restylekit.common.lifecycle.config.AsyncHookConfig

import scala.util.control.NoStackTrace
import scala.concurrent.TimeoutException

object AsyncHook {
  def apply(hooks : Set[Hook])(cfg : Config)(implicit shifter : ContextShift[IO], timer : Timer[IO]) : Hook = new Hook {

    import IO.shift

    import cats.instances.unit._
    import cats.instances.list._

    import cats.syntax.flatMap._
    import cats.syntax.foldable._

    import net.ceedubs.ficus.Ficus._
    import scg.restylekit.common.lifecycle.config.AsyncHookConfig.prefix

    val AsyncHookConfig(startupTimeout, shutdownTimeout) = cfg.as[AsyncHookConfig](prefix)

    override def startup() : IO[Unit] = {
      hooks.toList
        .foldMapM(hook => shift >> hook.startup())
        .timeoutTo(startupTimeout, timeoutError("Startup time is run out"))
    }

    override def shutdown() : IO[Unit] = {
      hooks.toList
        .foldMapM(hook => shift >> hook.shutdown())
        .timeoutTo(shutdownTimeout, timeoutError("Shutdown time is run out"))
    }
  }

  private[this] def timeoutError(message : String) : IO[Unit] = {
    IO.raiseError(e = new TimeoutException(message) with NoStackTrace)
  }
}
