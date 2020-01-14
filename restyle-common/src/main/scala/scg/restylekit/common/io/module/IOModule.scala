package scg.restylekit.common.io.module

import cats.effect.{ContextShift, IO, Timer}

import scg.restylekit.common.io.utils.MdcPropagationShift
import scg.restylekit.common.execution.module.ExecutionModule

trait IOModule {
  implicit def contextShift : ContextShift[IO]
  implicit def timer : Timer[IO]
}

trait IOModuleImpl extends IOModule {

  this : ExecutionModule =>

  import com.softwaremill.macwire.wire

  override implicit lazy val contextShift : ContextShift[IO] = wire[MdcPropagationShift]

  override implicit lazy val timer : Timer[IO] = IO.timer(context)

}
