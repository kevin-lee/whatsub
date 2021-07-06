package whatsub

import cats.syntax.show.*

/** @author Kevin Lee
  * @since 2021-06-18
  */
enum ConversionError {
  case NoContent(supportedSub: SupportedSub, subtitleInfo: String)
}
object ConversionError {
  extension (conversionError: ConversionError) {
    def render: String = conversionError match {
      case ConversionError.NoContent(supportedSub, subtitleInfo) =>
        s"$subtitleInfo of type ${supportedSub.show} has no content."
    }
  }
}
