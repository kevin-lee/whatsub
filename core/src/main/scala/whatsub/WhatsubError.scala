package whatsub

import cats.Show
import cats.syntax.all.*

import java.io.File

/** @author Kevin Lee
  * @since 2021-06-30
  */
enum WhatsubError {
  case ConversionFailure(conversionError: ConversionError)
  case ParseFailure(parseError: ParseError)

  case NoConversion(supportedSub: SupportedSub)

  case FileWriteFailure(file: File, error: Throwable)
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
    }
  }
}