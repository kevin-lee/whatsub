package whatsub.charset

import cats.Show

/**
 * @author Kevin Lee
 * @since 2021-08-15
 */
enum CharsetConvertError {
  case Conversion(from: ConvertCharset.From, to: ConvertCharset.To, inputInfo: String, error: Throwable)

  case Consumption(converted: String, error: Throwable)
}
