package scg.restylekit.common.temporal.model

import java.time.{Clock, Instant, LocalDate, LocalDateTime, LocalTime, MonthDay, OffsetDateTime, OffsetTime, Year, YearMonth, ZoneId, ZoneOffset, ZonedDateTime}

case class Temporal(clock : Clock = Clock.systemDefaultZone()) extends AnyVal

object Temporal {

  type TemporalConversion[R] = (Temporal => R)

  def apply[R : TemporalConversion] : R = Temporal()

  implicit lazy val toClock : TemporalConversion[Clock] = {
    case Temporal(clock) => clock
  }

  implicit lazy val toYear : TemporalConversion[Year] = {
    case Temporal(clock) => Year.now(clock)
  }

  implicit lazy val toZoneId : TemporalConversion[ZoneId] = {
    case Temporal(clock) => clock.getZone
  }

  implicit lazy val toInstant : TemporalConversion[Instant] = {
    case Temporal(clock) => Instant.now(clock)
  }

  implicit lazy val toMonthDay : TemporalConversion[MonthDay] = {
    case Temporal(clock) => MonthDay.now(clock)
  }

  implicit lazy val toYearMonth : TemporalConversion[YearMonth] = {
    case Temporal(clock) => YearMonth.now(clock)
  }

  implicit lazy val toLocalDate : TemporalConversion[LocalDate] = {
    case Temporal(clock) => LocalDate.now(clock)
  }

  implicit lazy val toLocalTime : TemporalConversion[LocalTime] = {
    case Temporal(clock) => LocalTime.now(clock)
  }

  implicit lazy val toZoneOffset : TemporalConversion[ZoneOffset] = {
    case Temporal(clock) => ZoneOffset.from(clock.instant())
  }

  implicit lazy val toOffsetTime : TemporalConversion[OffsetTime] = {
    case Temporal(clock) => OffsetTime.now(clock)
  }

  implicit lazy val toLocalDateTime : TemporalConversion[LocalDateTime] = {
    case Temporal(clock) => LocalDateTime.now(clock)
  }

  implicit lazy val toZonedDateTime : TemporalConversion[ZonedDateTime] = {
    case Temporal(clock) => ZonedDateTime.now(clock)
  }

  implicit lazy val toOffsetDateTime : TemporalConversion[OffsetDateTime] = {
    case Temporal(clock) => OffsetDateTime.now(clock)
  }

}
