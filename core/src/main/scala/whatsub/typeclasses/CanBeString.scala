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
  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  given nioPathCanBeString: CanBeString[Path] = _.toString
}
