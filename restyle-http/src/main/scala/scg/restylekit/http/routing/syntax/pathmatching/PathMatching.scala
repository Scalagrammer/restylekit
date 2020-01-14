package scg.restylekit.http.routing.syntax.pathmatching

import akka.http.scaladsl.server.PathMatcher1
import akka.http.scaladsl.server.PathMatchers.Segment
import play.api.libs.json.Format
import scg.restylekit.model.wrapper.{Wrapper, WrapperCompanion}

import scala.language.implicitConversions
import scala.util.Try

trait PathMatching[W <: Wrapper] {

  this : WrapperCompanion[W] =>

  def segment(s : String) : W

}

trait PlayJsonFormat[W <: Wrapper] {

  self : WrapperCompanion[W] =>

  implicit def jsonFormat(implicit valueFormat : Format[W#Val]) : Format[W] = {
    Format(fjs = valueFormat.map(self(_)),
           tjs = (w => valueFormat.writes(w.value)))
  }
}

object PathMatching {
  implicit def toPathMatcher[W <: Wrapper](matching : PathMatching[W]) : PathMatcher1[W] = {
    Segment.flatMap(s => Try(matching.segment(s)).toOption)
  }
}