package whatsub

import cats.Show
import cats.parse.{Parser as P}

enum ParseError {
  case SmiParseError(error: P.Error)
  case SrtParseError(lineIndex: Int, lineStr: String, error: P.Error)
}
object ParseError {

  given parseShow: Show[ParseError] = _.render

  extension (parseError: ParseError) {
    def render: String = parseError match {
      case SmiParseError(error) =>
        s"""SmiParseError:
           |- error: ${error.toString}
           |""".stripMargin

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
