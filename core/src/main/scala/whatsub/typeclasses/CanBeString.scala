package whatsub.typeclasses

import java.nio.file.Path

/** @author Kevin Lee
  * @since 2022-04-15
  */
trait CanBeString[A] {
  extension (a: A) {
    def stringValue: String
  }
}

object CanBeString {
  given nioPathCanBeString: CanBeString[Path] = String.valueOf(_)
}
