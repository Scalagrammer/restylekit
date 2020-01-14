package scg.restylekit.common.ops

import java.util.UUID

object StringExt {
  implicit class RichString(private val s : String) extends AnyVal {
    def toOption : Option[String] = if (s.trim.isEmpty) None else Some(s)
    def toUuid : UUID = UUID.fromString(s.trim)
  }
}
