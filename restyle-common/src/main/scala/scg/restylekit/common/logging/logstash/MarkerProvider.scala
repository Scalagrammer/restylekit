package scg.restylekit.common.logging.logstash

import org.slf4j.Marker
import com.typesafe.scalalogging.LazyLogging
import ch.qos.logback.classic.spi.ILoggingEvent
import com.fasterxml.jackson.core.JsonGenerator

import net.logstash.logback.marker.LogstashMarker
import net.logstash.logback.composite.AbstractJsonProvider

import scala.util.control.NonFatal

class MarkerProvider extends AbstractJsonProvider[ILoggingEvent] with LazyLogging {

  override def writeTo(generator : JsonGenerator, event : ILoggingEvent) : Unit = writeMarker(generator, event.getMarker)

  private def writeMarker(generator: JsonGenerator, marker: Marker): Unit = {
    // imperative scope-like for better performance
    try if (marker != null) {

      if (marker.isInstanceOf[LogstashMarker])
        marker.asInstanceOf[LogstashMarker].writeTo(generator)

      val markers = marker.iterator()

      while (markers.hasNext) writeMarker(generator, markers.next())

    } catch {
      case NonFatal(cause) => logger.error(s"An error has occurred during write logstash marker: $marker", cause)
    }
  }
}
