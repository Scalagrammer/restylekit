package scg.restylekit.common.syntax

import cats.data.{Ior, NonEmptyList}

package object data {

  type ^[A, B] = Ior[A, B]
  type |[A, B] = Either[A, B]
  type ->[A, B] = (A, B)

  object Nel {
    def unapply[A](nel : NonEmptyList[A]) : Option[List[A]] = Some(nel.toList)
  }

  object -> {
    def unapply[A, B](assoc : A -> B) : Option[A -> B] = Some(assoc)
  }

}
