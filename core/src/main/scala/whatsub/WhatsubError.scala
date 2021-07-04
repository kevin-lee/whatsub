package whatsub

import cats.Show

/** @author Kevin Lee
  * @since 2021-06-30
  */
enum WhatsubError {
  case ConversionFailure(conversionError: ConversionError)
  case ParseFailure(parseError: ParseError)
}

object WhatsubError {

  given whatsubErrorShow: Show[WhatsubError] = _.render

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
    }
  }
}