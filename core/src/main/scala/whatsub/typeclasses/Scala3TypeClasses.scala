package whatsub.typeclasses

import cats.{Eq, Show}

import java.nio.file.Path

/** @author Kevin Lee
  * @since 2022-04-15
  */
object Scala3TypeClasses {
  given scala3Eq[A](using CanEqual[A, A]): Eq[A] = Eq.fromUniversalEquals

}
