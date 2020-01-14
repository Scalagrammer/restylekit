package scg.restylekit.common.appstarter

import cats.effect.IO.suspend
import cats.effect.IO
import com.typesafe.scalalogging.Logger

import scg.restylekit.common.appstarter.AppStarter.printLogo
import scg.restylekit.common.config.module.ConfigModule.restyleVersion
import scg.restylekit.common.actors.module.ActorsModuleImpl
import scg.restylekit.common.config.module.ConfigModuleImpl
import scg.restylekit.common.execution.module.{ExecutionModule, ExecutionModuleImpl}
import scg.restylekit.common.io.module.{IOModule, IOModuleImpl}
import scg.restylekit.common.lifecycle.Hook

import scala.sys.ShutdownHookThread

trait AppStarter {

  this : Hook
    with IOModule
    with ExecutionModule =>

  import cats.syntax.apply._
  import cats.syntax.flatMap._

  import scg.restylekit.common.syntax.impure._

  def run() : IO[ExitCode]

  final def main(args : Array[String]) : Unit = {
    (printLogo() *> prepare(run()) >>= (_.join)).unsafeRunSync()
  } match {
    case ExitCode(0) =>
      ()
    case ExitCode(code) =>
      sys.exit(code)
  }

  final def registerShutdownHook() : IO[ShutdownHookThread] = {
    IO(sys.addShutdownHook(shutdown().unsafeRunSync()))
  }

  private[this] def prepare(kick : => IO[ExitCode]) = {

    def onError(cause : Throwable) : ExitCode = {
      ExitCode(1) <<* Logger[AppStarter].error("An error has occurred during AppStarter#run method", cause)
    }

    for {
      kickFiber <- suspend(kick).redeem(onError, identity).start
              _ <- IO(sys.addShutdownHook(kickFiber.cancel.unsafeRunSync()))
    } yield {
      kickFiber
    }
  }
}

object AppStarter {
  private[appstarter] def printLogo() : IO[Unit] = IO {
    println {
      ("\u001B[35m" +
        """|🅿🅾🆆🅴🆁🅴🅳 🅱🆈
           | _____          _         _
           || ___ \        | |       | | v
           || |_/ /___  ___| |_ _   _| | ___
           ||    // _ \/ __| __| | | | |/ _ \
           || |\ \  __/\__ \ |_| |_| | |  __/
           |\_| \ \___//___/\__|\__, |_|\___/
           |     \ \            __/ /
           |      \ |          |___/
           |       \|""".stripMargin + "\u001B[0m\n").replaceAll("v", restyleVersion)
    }
  }
}

trait AppStarterKit
  extends Hook
    with AppStarter
    with IOModuleImpl
    with ActorsModuleImpl
    with ConfigModuleImpl
    with ExecutionModuleImpl {

  import cats.syntax.functor._

  override def shutdown() : IO[Unit] = {
    IO.fromFuture(IO(actors.terminate())).as()
  }
}

case class ExitCode private[appstarter](code : Int)

object ExitCode {
  def apply(code : Int) : ExitCode = new ExitCode(code & 0xFF)
}