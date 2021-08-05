package whatsub

import SubParsers.*
import cats.effect.*
import cats.parse.Rfc5234.*
import cats.parse.{Parser as P, Parser0 as P0, *}
import cats.syntax.all.*
import cats.{Functor, Monad}
import effectie.cats.*
import effectie.cats.Effectful.*
import effectie.cats.EitherTSupport.*

object SmiParser {

  val NoNewlineChars = (0.toChar to Char.MaxValue).filter(c => c != '\n' && c != '\r')

  final case class StartAndLine(start: Long, line: String)

  final case class SmiLine(line: String)

  val samiSatartP = (P.ignoreCase("<SAMI>") <* (lwsp.string.? ~ newlineP.?)).map(s => SmiComponent.SamiStart)
  val styleP      = (spaceP | P.string("<!--") | P.string("-->") | (alpha | digit | P.charIn(".{}-:;")).rep.string)
  val headP       =
    (
      (
        (P.ignoreCase("<HEAD>") ~ lwsp.? ~ newlineP.? ~ P.ignoreCase("<TITLE>")) *>
          P.charWhere(_ != '<').rep.string.? <*
          (P.ignoreCase("</TITLE>") ~ lwsp.? ~ newlineP.? ~ P.ignoreCase(
            """<STYLE TYPE="text/css">""",
          ) ~ lwsp.? ~ newlineP.?)
      ) ~ ((styleP.rep.string.backtrack ~ newlineP.?).rep.string <*
        (P.ignoreCase("</STYLE>") ~ lwsp.? ~ newlineP.? ~ P.ignoreCase("</HEAD>") ~ lwsp.? ~ newlineP.?))
    )
      .map {
        case (title, style) =>
          SmiComponent.Head(SmiComponent.Title(title.getOrElse("")))
      }

  val bodyStartP = (P.ignoreCase("<BODY>") <* (lwsp.string.? ~ newlineP.?)).map(_ => SmiComponent.BodyStart)

  val bodyLine = (
    (
      P.ignoreCase("<SYNC Start=") *> digit.rep.string <* (P
        .ignoreCase("><P") ~ (wsp ~ P.ignoreCase("Class=") ~ alpha.rep.string).? ~ P.ignoreCase(
        ">",
      ))
    ) ~ ((lwsp.string.?) *> P.charIn(SmiParser.NoNewlineChars).rep.string <* (lwsp.string.? ~ newlineP))
  )
    .map { case (startTime, line) => StartAndLine(startTime.toLong, line) }

  val bodyEndP = (P.ignoreCase("</BODY>") <* (lwsp.string.? ~ newlineP.?)).map(_ => SmiComponent.BodyEnd)
  val samiEndP = (P.ignoreCase("</SAMI>") <* (lwsp.string.? ~ newlineP.?)).map(_ => SmiComponent.SamiEnd)

  def parseSmiStart[F[_]: Fx: Monad](
    lines: String,
    acc: List[SmiComponent],
  ): F[Either[ParseError, List[SmiComponent]]] =
    effectOf(samiSatartP.parse(lines))
      .eitherT
      .leftMap(err => ParseError.SmiParseError(err))
      .flatMapF {
        case ((remaining, start)) =>
          parseSmiHead(remaining, start :: acc)
      }
      .value

  def parseSmiHead[F[_]: Fx: Monad](lines: String, acc: List[SmiComponent]): F[Either[ParseError, List[SmiComponent]]] =
    effectOf(headP.parse(lines))
      .eitherT
      .leftMap(err => ParseError.SmiParseError(err))
      .flatMapF {
        case ((remaining, head)) =>
          parseBodyStart(remaining, head :: acc)
      }
      .value

  def parseBodyStart[F[_]: Fx: Monad](
    lines: String,
    acc: List[SmiComponent],
  ): F[Either[ParseError, List[SmiComponent]]] =
    effectOf(bodyStartP.parse(lines))
      .eitherT
      .leftMap(err => ParseError.SmiParseError(err))
      .flatMapF {
        case ((remaining, bodyStart)) =>
          parseBody(remaining, bodyStart :: acc)
      }
      .value

  def parseBody[F[_]: Fx: Monad](lines: String, acc: List[SmiComponent]): F[Either[ParseError, List[SmiComponent]]] = {

    def parseNextLine(
      lines: String,
      previous: Option[StartAndLine],
      acc: List[SmiComponent],
    ): F[Either[ParseError, List[SmiComponent]]] =
      effectOf(bodyEndP.parse(lines))
        .flatMap {
          case Right((remaining, bodyEnd)) =>
            parseSamiEnd(remaining, bodyEnd :: acc)

          case Left(_) =>
            bodyLine.parse(lines) match {
              case Right((remaining, StartAndLine(time, line))) =>
                previous match {
                  case Some(StartAndLine(startTime, theLine)) =>
                    parseNextLine(
                      remaining,
                      none,
                      SmiComponent.BodyLine(
                        SmiComponent.Milliseconds(startTime.toLong),
                        SmiComponent.Milliseconds(time.toLong),
                        SmiComponent.Line(theLine),
                      ) :: acc,
                    )

                  case None =>
                    parseNextLine(
                      remaining,
                      StartAndLine(time.toLong, line).some,
                      acc,
                    )
                }

              case Left(err) =>
                effectOf(ParseError.SmiParseError(err).asLeft)
            }
        }

    parseNextLine(lines, none, acc)
  }

  def parseSamiEnd[F[_]: Fx: Monad](lines: String, acc: List[SmiComponent]): F[Either[ParseError, List[SmiComponent]]] =
    effectOf(samiEndP.parse(lines))
      .map {
        case Left(err)                  =>
          ParseError.SmiParseError(err).asLeft
        case Right((remining, samiEnd)) =>
          (SmiComponent.SamiEnd :: acc).asRight
      }

  private def fromSmiComponents(smiComponents: List[SmiComponent]): Smi =
    smiComponents.foldRight((none[Smi.Title], List.empty[Smi.SmiLine])) {
      case (smiComponent, (maybeTitle, acc)) =>
        smiComponent match {
          case SmiComponent.BodyLine(start, end, line) =>
            (
              maybeTitle,
              Smi.SmiLine(
                Smi.Start(start.value),
                Smi.End(end.value),
                Smi.Line(line.value),
              ) :: acc,
            )

          case SmiComponent.Head(title) =>
            (Smi.Title(title.value).some, acc)

          case SmiComponent.SamiStart | SmiComponent.BodyStart | SmiComponent.BodyEnd | SmiComponent.SamiEnd =>
            (maybeTitle, acc)
        }
    } match {
      case (Some(title), lines) =>
        Smi(title, lines)

      case (None, lines) =>
        Smi(Smi.Title(""), lines)
    }

  def parse[F[_]: Fx: Monad](lines: String): F[Either[ParseError, Smi]] =
    parseSmiStart(lines.removeEmptyChars, List.empty)
      .eitherT
      .map(smiComponents => fromSmiComponents(smiComponents.reverse))
      .value

}
