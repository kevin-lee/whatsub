package whatsub

/** @author Kevin Lee
  * @since 2021-06-18
  */
enum ConversionError {
  case NoContent(subtitleInfo: String)
}
object ConversionError {
  extension (conversionError: ConversionError) {
    def render: String = conversionError match {
      case ConversionError.NoContent(subtitleInfo) =>
        s"$subtitleInfo has no content."
    }
  }
}
