package whatsub

import cats.Show
import cats.parse.{Parser as P}

enum ParseError {
  case SrtParseError(lineIndex: Int, lineStr: String, error: P.Error)
}
object ParseError {

  given parseShow: Show[ParseError] = _.render

  extension (parseError: ParseError) {
    def render: String = parseError match {
      case SrtParseError(lineIndex, lineStr, error) =>
        s"""SrtParseError:
           |- lineIndex: $lineIndex
           |- line:
           |---
           |$lineStr
           |---
           |- error: ${error.toString}
           |""".stripMargin
    }
  }

}
