package scg.restylekit.model.wrapper

import java.util.UUID

trait Wrapper extends Any {
  this : AnyVal => type Val

  def value : Val

  override def toString : String = value.toString
}

trait IntWrapper extends Any with Wrapper {
  this : AnyVal =>

  override type Val = Int
}

trait StringWrapper extends Any with Wrapper {
  this : AnyVal =>

  override type Val = String
}

trait UuidWrapper extends Any with Wrapper {
  this : AnyVal =>

  override type Val = UUID
}

trait LongWrapper extends Any with Wrapper {
  this : AnyVal =>

  override type Val = Long
}

trait BooleanWrapper extends Any with Wrapper {
  this : AnyVal =>

  override type Val = Boolean
}


trait WrapperCompanion[W <: Wrapper] {
  def apply(value : W#Val) : W
}

object WrapperCompanion {
  def apply[W <: Wrapper](implicit companion : WrapperCompanion[W]) : WrapperCompanion[W] = companion
}
