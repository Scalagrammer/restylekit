package scg.restylekit.http.syntax

import akka.http.scaladsl.model.Uri

package object uri {

  implicit class RichUri(private val uri : Uri) extends AnyVal {
    def / (path : String) : Uri = uri.withPath(uri.path / path)
  }

}
