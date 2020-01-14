package scg.restylekit.model

import java.util.UUID

import scg.restylekit.model.wrapper.{StringWrapper, UuidWrapper, WrapperCompanion}

package object ids {

  case class Tag(value : String) extends AnyVal with StringWrapper

  implicit object Tag extends WrapperCompanion[Tag]

  case class Id(value : UUID) extends AnyVal with UuidWrapper

  implicit object Id extends WrapperCompanion[Id]

}
