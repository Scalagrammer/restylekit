package scg.restylekit.common.io.utils

import org.slf4j.MDC

import java.util.{Map => <>}

import cats.effect.{ContextShift, IO}
import scg.restylekit.common.syntax.data.|

import scala.concurrent.ExecutionContext

class MdcPropagationShift(implicit mainContext : ExecutionContext) extends ContextShift[IO] {

  type ContextMap = String <> String

  type Callback[A] = (Throwable | A) => Unit

  override def shift : IO[Unit] = {
    val mdc = MDC.getCopyOfContextMap ; IO.async(withinMain(mdc, rightUnit))
  }

  override def evalOn[A](evalContext : ExecutionContext)(fa : IO[A]) : IO[A] = {
    val mdc = MDC.getCopyOfContextMap ; IO.async { callback =>
      evalContext.execute(withinMdc(mdc, withinMain(mdc, fa.attempt.unsafeRunSync())(callback)))
    }
  }

  private[this] def withinMain[A](mdc : ContextMap, result : (Throwable | A))(callback : Callback[A]) : Unit = {
    mainContext.execute(withinMdc(mdc, callback(result)))
  }

  private[this] def withinMdc[A](mdc : ContextMap, call : => Unit) : Runnable = { () =>
    val backup = backupAndSwap(mdc) ; try call finally revert(backup)
  }

  private[this] def backupAndSwap(mdc : ContextMap) : ContextMap = {
    val backup = MDC.getCopyOfContextMap ; if (mdc != null) MDC.setContextMap(mdc) ; backup
  }

  private[this] def revert(mdcBackup : ContextMap) : Unit = {
    if (mdcBackup != null) MDC.setContextMap(mdcBackup)
  }

  private[this] val rightUnit = Right()

}
