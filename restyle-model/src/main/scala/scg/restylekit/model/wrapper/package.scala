package scg.restylekit.model

import java.util.UUID

package object wrapper {
  implicit class LongWrap(private val value : Long) extends AnyVal {
    def wrap[LW <: LongWrapper : WrapperCompanion] : LW = implicitly[WrapperCompanion[LW]].apply(value)
  }

  implicit class UuidWrap(private val value : UUID) extends AnyVal {
    def wrap[UW <: UuidWrapper : WrapperCompanion] : UW = implicitly[WrapperCompanion[UW]].apply(value)
  }

  implicit class StringWrap(private val value : String) extends AnyVal {
    def wrap[SW <: StringWrapper : WrapperCompanion] : SW = implicitly[WrapperCompanion[SW]].apply(value)
  }

  implicit class IntWrap(private val value : Int) extends AnyVal {
    def wrap[IW <: IntWrapper : WrapperCompanion] : IW = implicitly[WrapperCompanion[IW]].apply(value)
  }

  implicit class BooleanWrap(private val value : Boolean) extends AnyVal {
    def wrap[BW <: BooleanWrapper : WrapperCompanion] : BW = implicitly[WrapperCompanion[BW]].apply(value)
  }
}
