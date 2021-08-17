package whatsub

import cats.Show
import cats.syntax.all.*
import whatsub.charset.{CharsetConvertError, ConvertCharset}
import whatsub.convert.ConversionError
import whatsub.parse.ParseError

import java.io.File

/** @author Kevin Lee
  * @since 2021-06-30
  */
enum WhatsubError {
  case ConversionFailure(conversionError: ConversionError)

  case ParseFailure(parseError: ParseError)

  case NoConversion(supportedSub: SupportedSub)

  case FileWriteFailure(file: File, error: Throwable)

  case FailedWithExitCode(exitCode: Int)

  case CharsetConversion(charsetConvertError: CharsetConvertError)
}

object WhatsubError {

  given whatsubErrorShow: Show[WhatsubError] = _.toString

  extension (whatsubError: WhatsubError) {
    def render: String = whatsubError match {
      case WhatsubError.ConversionFailure(conversionError) =>
        s"""Conversion Failed:
           |${conversionError.render}
           |""".stripMargin

      case WhatsubError.ParseFailure(parseError) =>
        s"""Parsing sub failed:
           |${parseError.render}
           |""".stripMargin

      case NoConversion(supportedSub) =>
        s"""No conversion: The subtitle to convert from and to are the same (i.e. ${supportedSub.show})"""

      case FileWriteFailure(file: File, error: Throwable) =>
        s"""Writing file at ${file.getCanonicalPath} has failed with ${error.getMessage}"""

      case FailedWithExitCode(exitCode) =>
        s"Failed with exit code ${exitCode.toString}"

      case CharsetConversion(
            CharsetConvertError.Conversion(from, to, input, error),
          ) =>
        s"""Error when converting charset
           | From: $from
           |   To: $to
           |input: $input
           |error: ${error.getMessage}
           |""".stripMargin

      case CharsetConversion(
            CharsetConvertError.Consumption(converted, error),
          ) =>
        s"""Error when consuming converted subtitle content
           |converted: $converted
           |    error: ${error.getMessage}
           |""".stripMargin

    }
  }
}
