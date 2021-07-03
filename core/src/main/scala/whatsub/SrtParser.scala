package whatsub

import SubParsers.*
import cats.data.NonEmptyList
import cats.effect.*
import cats.parse.Rfc5234.*
import cats.parse.{Parser as P, Parser0 as P0, *}
import cats.syntax.all.*

/** @author Kevin Lee
  * @since 2021-07-03
  */
object SrtParser {

  val indexP = digit.rep.string.map(_.toInt)

  val twoDigits   = digit.rep(2, 2).string
  val threeDigits = digit.rep(3, 3).string
  val playtimeP   =
    (
      twoDigits ~
        (P.string(":") *> twoDigits) ~
        (P.string(":") *> twoDigits) ~
        (P.string(",") *> threeDigits)
    ).map {
      case (((h, m), s), ms) =>
        Playtime(
          Playtime.Hours(h.toInt),
          Playtime.Minutes(m.toInt),
          Playtime.Seconds(s.toInt),
          Playtime.Milliseconds(ms.toInt),
        )
    }

  val arrowP = P.string("-->")

  val playtimeRangeP: P[(Playtime, Playtime)] =
    (playtimeP ~ ((spaceP.rep ~ arrowP ~ spaceP.rep) *> playtimeP) <* (spaceP.? ~ newlineP))

  def parseSrtLine(line: String, lineIndex: Int): Either[ParseError, Srt.SrtLine] =
    ((indexP <* (spaceP.? ~ newlineP)) ~ playtimeRangeP)
      .parse(line) match {
      case Right((remaining, ((index, (playtimeStart, playtimeEnd))))) =>
        Srt
          .SrtLine(
            Srt.Index(index),
            Srt.Start(playtimeStart.toMilliseconds),
            Srt.End(playtimeEnd.toMilliseconds),
            Srt.Line(remaining),
          )
          .asRight

      case Left(err) =>
        ParseError.SrtParseError(lineIndex, line, err).asLeft
    }

  def parse(lines: String): Either[ParseError, Srt] =
    lines
      .removeEmptyChars
      .split("[\r\n]{2}")
      .zipWithIndex
      .toList
      .traverse(parseSrtLine)
      .map(Srt.apply)

}
