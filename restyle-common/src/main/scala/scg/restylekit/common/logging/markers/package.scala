package scg.restylekit.common.logging

import cats.effect.Sync
import com.typesafe.scalalogging.Logger
import net.logstash.logback.marker.{LogstashMarker, Markers => LogstashMarkers}
import scg.restylekit.common.syntax.data.->

import scala.language.implicitConversions

package object markers {

  type Logs = Map[String, Any]

  def logs(markers : (String, Any)*) : Logs = markers.toMap

  implicit class LogsExt(private val logs : Logs) extends AnyVal {
    def withMaybeMarker(maybeMarker : (String -> Option[Any])) : Logs = {
      (maybeMarker._2)
        .map(value => logs + (maybeMarker._1 -> value))
        .getOrElse(logs)
    }

    def withMarkers(maybeMarker : (String -> TraversableOnce[Any])) : Logs = {
      if (maybeMarker._2.isEmpty) logs else logs + (maybeMarker._1 -> maybeMarker._2.mkString(","))
    }
  }

  implicit class RichLogger(private val logger : Logger) extends AnyVal {

    import implicits._

    import scala.language.higherKinds

    def errorF[F[_] : Sync](marker : (String -> Any), message : String) : F[Unit] = errorF(Map(marker), message)
    def errorF[F[_] : Sync](marker : (String -> Any), message : String, cause : Throwable) : F[Unit] = errorF(Map(marker), message, cause)
    def errorF[F[_] : Sync](context : Logs, message : String) : F[Unit] = Sync[F].delay(error(context, message))
    def errorF[F[_] : Sync](message : String, cause : Throwable) : F[Unit] = Sync[F].delay(logger.error(message, cause))
    def errorF[F[_] : Sync](message : String) : F[Unit] = Sync[F].delay(logger.error(message))
    def errorF[F[_] : Sync](context : Logs, message : String, cause : Throwable) : F[Unit] = Sync[F].delay(error(context, message, cause))

    def error(marker : (String -> Any), message : String) : Unit = error(Map(marker), message)
    def error(marker : (String -> Any), message : String, cause : Throwable) : Unit = error(Map(marker), message, cause)
    def error(context : Logs, message : String) : Unit = logger.error(context, message)
    def error(context : Logs, message : String, cause : Throwable) : Unit = logger.error(context, message, cause)


    def infoF[F[_] : Sync](message : String) : F[Unit] = Sync[F].delay(logger.info(message))
    def infoF[F[_] : Sync](marker  : (String -> Any), message : String) : F[Unit] = infoF(Map(marker), message)
    def infoF[F[_] : Sync](context : Logs, message : String) : F[Unit] = Sync[F].delay(info(context, message))

    def info(marker  : (String -> Any), message : String) : Unit = info(Map(marker), message)
    def info(context : Logs, message : String) : Unit = logger.info(context, message)


    def warnF[F[_] : Sync](message : String) : F[Unit] = Sync[F].delay(logger.warn(message))
    def warnF[F[_] : Sync](marker  : (String -> Any), message : String) : F[Unit] = warnF(Map(marker), message)
    def warnF[F[_] : Sync](context : Logs, message : String) : F[Unit] = Sync[F].delay(warn(context, message))

    def warn(marker  : (String -> Any), message : String) : Unit = warn(Map(marker), message)
    def warn(context : Logs, message : String) : Unit = logger.warn(context, message)


    def debugF[F[_] : Sync](message : String) : F[Unit] = Sync[F].delay(logger.debug(message))
    def debugF[F[_] : Sync](marker  : (String -> Any), message : String) : F[Unit] = debugF(Map(marker), message)
    def debugF[F[_] : Sync](context : Logs, message : String) : F[Unit] = Sync[F].delay(debug(context, message))

    def debug(marker  : (String -> Any), message : String) : Unit = debug(Map(marker), message)
    def debug(context : Logs, message : String) : Unit = logger.debug(context, message)


    def traceF[F[_] : Sync](message : String) : F[Unit] = Sync[F].delay(logger.trace(message))
    def traceF[F[_] : Sync](marker  : (String -> Any), message : String) : F[Unit] = traceF(Map(marker), message)
    def traceF[F[_] : Sync](context : Logs, message : String) : F[Unit] = Sync[F].delay(logger.trace(context, message))

    def trace(marker  : (String -> Any), message : String) : Unit = trace(Map(marker), message)
    def trace(context : Logs, message : String) : Unit = logger.trace(context, message)

  }

  object implicits {
    implicit def toMarker(markers : Logs) : LogstashMarker = {

      import scala.collection.JavaConverters._

      LogstashMarkers.appendEntries {
        markers.mapValues(String.valueOf).asJava
      }
    }
  }
}
