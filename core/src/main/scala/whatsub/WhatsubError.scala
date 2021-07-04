package whatsub

import cats.Show
import cats.syntax.all.*

/** @author Kevin Lee
  * @since 2021-06-30
  */
enum WhatsubError {
  case ConversionFailure(conversionError: ConversionError)
  case ParseFailure(parseError: ParseError)

  case NoConversion(supportedSub: SupportedSub)
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
    }
  }
}