package whatsub.parse

import cats.Show
import cats.parse.Parser as P

enum ParseError {
  case SmiParseError(lineIndex: Int, lineStr: String, error: P.Error)
  case SmiParseInvalidLineError(lineIndex: Int, lineStr: String, error: String)
  case SrtParseError(lineIndex: Int, lineStr: String, additionalInfo: Option[String], error: P.Error)
  case SrtParseInvalidLineError(lineIndex: Int, lineStr: String, error: String)
}
object ParseError {

  given parseShow: Show[ParseError] = _.render

  extension (parseError: ParseError) {
    def render: String = parseError match {
      case SmiParseError(lineIndex, lineStr, error) =>
        s"""SmiParseError:
           |- lineIndex: $lineIndex
           |- line:
           |---
           |$lineStr
           |---
           |- error: ${error.toString}
           |""".stripMargin

      case SmiParseInvalidLineError(lineIndex, lineStr, error) =>
        s"""SrtParseError:
           |- lineIndex: $lineIndex
           |- line:
           |---
           |$lineStr
           |---
           |- error: $error
           |""".stripMargin

      case SrtParseError(lineIndex, lineStr, additionalInfo, error) =>
        s"""SrtParseError:
           |- lineIndex: $lineIndex
           |- line:
           |---
           |$lineStr
           |---
           |additionalInfo: ${additionalInfo.getOrElse("Nothing")}
           |- error: ${error.toString}
           |""".stripMargin

      case SrtParseInvalidLineError(lineIndex, lineStr, error) =>
        s"""SrtParseError:
           |- lineIndex: $lineIndex
           |- line:
           |---
           |$lineStr
           |---
           |- error: $error
           |""".stripMargin
    }
  }

}
