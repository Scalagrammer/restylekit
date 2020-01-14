package scg.restylekit.common.lifecycle

import cats.effect.IO

trait Hook {
  def startup()  : IO[Unit] = IO.unit
  def shutdown() : IO[Unit] = IO.unit
}

object ShutdownHook {
  def apply(hook : => Unit) : Hook = new Hook {
    override def shutdown() : IO[Unit] = IO(hook)
  }

  def apply(f : IO[Unit]) : Hook = new Hook {
    override def shutdown() : IO[Unit] = IO.suspend(f)
  }
}

object StartupHook {
  def apply(hook : => Unit) : Hook = new Hook {
    override def startup() : IO[Unit] = IO(hook)
  }

  def apply(f : IO[Unit]) : Hook = new Hook {
    override def startup() : IO[Unit] = IO.suspend(f)
  }
}
