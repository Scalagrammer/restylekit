package scg.restylekit.http.routing.response

import scg.restylekit.http.routing.syntax.pathmatching.PlayJsonFormat
import scg.restylekit.model.wrapper.{StringWrapper, WrapperCompanion}

package object model {

  case class ResponseCode(value : String) extends AnyVal with StringWrapper

  object ResponseCode extends WrapperCompanion[ResponseCode] with PlayJsonFormat[ResponseCode]

}