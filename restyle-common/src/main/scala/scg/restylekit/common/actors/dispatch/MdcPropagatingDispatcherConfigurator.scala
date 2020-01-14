package scg.restylekit.common.actors.dispatch

import java.util.{Map => <>}

import java.util.concurrent.TimeUnit.{MILLISECONDS, NANOSECONDS}

import org.slf4j.MDC
import akka.dispatch._
import com.typesafe.config.Config
import net.ceedubs.ficus.Ficus._

import scala.concurrent.ExecutionContext

import scala.concurrent.duration.{Duration, FiniteDuration}

// DO NOT TOUCH THIS!

class MdcPropagatingDispatcherConfigurator(cfg : Config, prs : DispatcherPrerequisites) extends MessageDispatcherConfigurator(cfg, prs) { self =>
  override val dispatcher : MessageDispatcher = new MdcPropagatingDispatcher(configurator = self,
                                                                              id = cfg.as[String]("id"),
                                                                              throughput = cfg.as[Int]("throughput"),
                                                                              executorServiceFactoryProvider = configureExecutor(),
                                                                              shutdownTimeout = FiniteDuration(cfg.getDuration("shutdown-timeout", MILLISECONDS), unit = MILLISECONDS),
                                                                              throughputDeadlineTime = FiniteDuration(cfg.getDuration("throughput-deadline-time", NANOSECONDS), unit = NANOSECONDS))
}

class MdcPropagatingDispatcher(configurator                   : MessageDispatcherConfigurator,
                               id                             : String,
                               throughput                     : Int,
                               throughputDeadlineTime         : Duration,
                               executorServiceFactoryProvider : ExecutorServiceFactoryProvider,
                               shutdownTimeout                : FiniteDuration) extends Dispatcher(configurator, id, throughput, throughputDeadlineTime, executorServiceFactoryProvider, shutdownTimeout) {

  dispatcher =>

  override def prepare() : ExecutionContext = new ExecutionContext {
    //
    val enclosed : (String <> String) = MDC.getCopyOfContextMap
    //
    override def execute(task : Runnable) : Unit = dispatcher.execute { () => // run propagation
      //
      val backup = MDC.getCopyOfContextMap
      //
      setMdc(enclosed)
      //
      try task.run() finally setMdc(backup)
    }

    override def reportFailure(cause : Throwable) : Unit = dispatcher.reportFailure(cause)
  }

  private[this] def setMdc(context : (String <> String)) : Unit = {
    if (context != null) MDC.setContextMap(context)
  }
}
