package whatsub.parse

import SubParsers.*
import cats.effect.*
import cats.parse.Rfc5234.*
import cats.parse.{Parser as P, Parser0 as P0, *}
import cats.syntax.all.*
import cats.{Functor, Monad}
import effectie.core.*
import effectie.syntax.all.*
import extras.cats.syntax.all.*
import whatsub.Smi

import scala.annotation.tailrec

/** @author Kevin Lee
  * @since 2021-08-15
  */
object SmiParser {

  val NoNewlineChars = (0.toChar to Char.MaxValue).filter(c => c != '\n' && c != '\r')

  final case class SyncInfo(sync: Long)
  final case class SyncStartEndInfo(start: Long, end: Long)
  final case class SyncInfoAndLine(sync: Long, line: String)
  final case class SyncStartEndInfoAndLine(start: Long, end: Long, line: String)
  final case class Line(line: String)

  type LineComponent = SyncInfoAndLine | SyncStartEndInfoAndLine | SyncInfo | SyncStartEndInfo | Line

  final case class StartAndLine(start: Long, line: String)

  val playtimeAndClassP: P[LineComponent] = (
    P.ignoreCase("<SYNC Start=") *>
      /* Start */
      digit.rep.string ~
      (
        wsp.rep ~ P.ignoreCase("End=") *>
          /* End */
          digit.rep.string
      ).? <* (P
        .ignoreCase("><P") ~ (wsp ~ P.ignoreCase("Class=") ~ alpha.rep.string).? ~ P
        .ignoreCase(">") ~ (wsp.rep | P.ignoreCase("&nbsp;").rep).string.rep0 ~ P.end)
  ).map {
    case (start, Some(end)) =>
      SyncStartEndInfo(start.toLong, end.toLong)
    case (playtime, None)   =>
      SyncInfo(playtime.toLong)
  }

  val playtimeOnlyP: P[LineComponent] =
    (P.ignoreCase("<SYNC Start=") *>
      /* Start */
      digit.rep.string ~
      (
        wsp.rep ~ P.ignoreCase("End=") *>
          /* End */
          digit.rep.string
      ).? <* ((P
        .ignoreCase(">") ~ (wsp.rep | P.ignoreCase("&nbsp;").rep).string.rep0 ~ P.end)))
      .map {
        case (start, Some(end)) =>
          SyncStartEndInfo(start.toLong, end.toLong)
        case (playtime, None)   =>
          SyncInfo(playtime.toLong)
      }

  val playtimeAndLine: P[LineComponent] = (
    (
      P.ignoreCase("<SYNC Start=") *>
        /* Start */
        digit.rep.string ~
        (wsp.rep ~ P.ignoreCase("End=") *>
          /* End */
          digit.rep.string).? <* (P
          .ignoreCase("><P") ~ (wsp ~ P.ignoreCase("Class=") ~ alpha.rep.string).? ~ P.ignoreCase(
          ">",
        ))
    ) ~ (
      (wsp.string.?) *>
        /* Line */
        P.anyChar.rep.string
        <* (wsp.string.? ~ P.end)
    )
  ).map {
    case ((start, Some(end)), line) =>
      SyncStartEndInfoAndLine(
        start.toLong,
        end.toLong,
        line,
      )
    case ((startTime, None), line)  =>
      SyncInfoAndLine(
        startTime.toLong,
        line,
      )
  }

  val justLineP: P[LineComponent] = (P.anyChar.rep.string <* P.end).map(Line(_))

  val lineP: P[LineComponent] =
    playtimeAndClassP.backtrack | playtimeOnlyP.backtrack | playtimeAndLine.backtrack | justLineP

  enum ParseStatus derives CanEqual {
    case SamiStart
    case SamiEnd
    case HeadStart
    case HeadEnd
    case TitleStart
    case TitleEnd
    case CommentStart
    case CommentEnd
    case StyleStart
    case StyleEnd
    case BodyStart
    case BodyEnd
  }

  def parse[F[*]: Fx: Monad](lines: Seq[String]): F[Either[ParseError, Smi]] =
    parseAll(lines.map(_.removeEmptyChars.trim).zipWithIndex, none, Vector.empty)
      .eitherT
      .map((title, lines) => Smi(title.getOrElse(Smi.Title("")), lines.toList))
      .value

  private def parseAll[F[*]: Fx: Monad](
    lineAndLineNums: Seq[(String, Int)],
    title: Option[Smi.Title],
    acc: Vector[Smi.SmiLine],
  ): F[Either[ParseError, (Option[Smi.Title], Vector[Smi.SmiLine])]] =
    (
      lineAndLineNums
        .traverse((line, index) => effectOf((line.removeEmptyChars.trim, index)))
        .map(_.filter((line, index) => line.nonEmpty)),
      )
      .flatMap {
        case (line, index) +: rest =>
          parseNonLine(line) match {
            case Some(ParseStatus.SamiStart)    =>
              parseAll(skipUntil(rest, ParseStatus.HeadStart), title, acc)
            case Some(ParseStatus.SamiEnd)      =>
              effectOf((title, acc).asRight)
            case Some(ParseStatus.HeadStart)    =>
              parseAll(skipUntil(rest, ParseStatus.TitleStart), title, acc)
            case Some(ParseStatus.HeadEnd)      =>
              parseAll(skipUntil(rest, ParseStatus.BodyStart), title, acc)
            case Some(ParseStatus.TitleStart)   =>
              parseTitle(
                (line.drop(SmiStr.TitleStart.length + 1), index) +: rest,
              )
                .rightT[ParseError]
                .flatMapF {
                  case (maybeTitle, rest) =>
                    parseAll(rest, maybeTitle.orElse(title), acc)
                }
                .value
            case Some(ParseStatus.TitleEnd)     =>
              parseAll(skipUntil(rest, ParseStatus.StyleStart), title, acc)
            case Some(ParseStatus.CommentStart) =>
              parseAll(skipUntil(rest, ParseStatus.CommentEnd), title, acc)
            case Some(ParseStatus.CommentEnd)   =>
              parseAll(rest, title, acc)
            case Some(ParseStatus.StyleStart)   =>
              val skippedToStyleEnd = skipUntil(rest, ParseStatus.StyleEnd)
              if skippedToStyleEnd.isEmpty then parseAll(rest, title, acc)
              else parseAll(skippedToStyleEnd, title, acc)
            case Some(ParseStatus.StyleEnd)     =>
              parseAll(skipUntil(rest, ParseStatus.HeadEnd), title, acc)
            case Some(ParseStatus.BodyStart)    =>
              parseAll(rest, title, acc)
            case Some(ParseStatus.BodyEnd)      =>
              parseAll(skipUntil(rest, ParseStatus.SamiEnd), title, acc)
            case None                           =>
              parseLine(lineAndLineNums, Vector.empty)
                .eitherT
                .flatMapF {
                  case (remaining, lines) =>
                    parseAll(remaining, title, acc ++ lines)
                }
                .value
          }

        case Seq() =>
          effectOf((title, acc).asRight)
      }

  @tailrec
  private def skipUntil(lineAndLineNums: Seq[(String, Int)], target: ParseStatus): Seq[(String, Int)] =
    lineAndLineNums match {
      case (line, index) +: rest =>
        val preprocessed = line.removeEmptyChars.trim
        parseNonLine(preprocessed)
          .find(_ == target) match {
          case None    =>
            skipUntil(rest, target)
          case Some(_) =>
            lineAndLineNums
        }

      case Seq() =>
        List.empty
    }

  private def parseLine[F[*]: Fx: Monad](
    lineAndLineNums: Seq[(String, Int)],
    acc: Vector[Smi.SmiLine],
  ): F[Either[ParseError, (Seq[(String, Int)], Vector[Smi.SmiLine])]] =
    lineAndLineNums match {
      case (line, lineNum) +: rest =>
        val preprocessed  = line.removeEmptyChars.trim
        val nonLineParsed = parseNonLine(preprocessed)
        nonLineParsed match {
          case Some(ParseStatus.CommentStart) =>
            parseLine(skipUntil(rest, ParseStatus.CommentEnd), acc)

          case Some(ParseStatus.CommentEnd) =>
            parseLine(rest, acc)

          case Some(_) =>
            effectOf((lineAndLineNums, acc).asRight[ParseError])

          case None =>
            lineP.parse(preprocessed) match {
              case Right((remaining, SyncInfoAndLine(start, line))) =>
                parseLineWithPrevious(
                  rest,
                  SyncInfoAndLine(start, line),
                  acc,
                )

              case Right((remaining, SyncStartEndInfoAndLine(start, end, line))) =>
                parseLine(
                  rest,
                  acc :+ Smi.SmiLine(Smi.Start(start), Smi.End(end), Smi.Line(line)),
                )

              case Right((remaining, SyncInfo(start))) =>
                parseLineWithPrevious(
                  rest,
                  SyncInfo(start),
                  acc,
                )

              case Right((remaining, SyncStartEndInfo(start, end))) =>
                parseLineWithPrevious(
                  rest,
                  SyncStartEndInfo(start, end),
                  acc,
                )

              case Right((remaining, Line(line))) =>
                effectOf(
                  ParseError
                    .SmiParseInvalidLineError(
                      lineNum,
                      line,
                      s"SMI line appears at unexpected position. [parsed line: $line, remaining: $remaining]",
                    )
                    .asLeft[(Seq[(String, Int)], Vector[Smi.SmiLine])],
                )

              case Left(err) =>
                effectOf(ParseError.SmiParseError(lineNum, line, err).asLeft[(Seq[(String, Int)], Vector[Smi.SmiLine])])
            }
        }
      case Seq()                   =>
        effectOf((lineAndLineNums, acc).asRight[ParseError])
    }

  private def parseLineWithPrevious[F[*]: Fx: Monad](
    lineAndLineNums: Seq[(String, Int)],
    previous: SyncInfoAndLine | SyncInfo | SyncStartEndInfo | SyncStartEndInfoAndLine,
    acc: Vector[Smi.SmiLine],
  ): F[Either[ParseError, (Seq[(String, Int)], Vector[Smi.SmiLine])]] = lineAndLineNums match {
    case (line, lineNum) +: rest =>
      val preprocessed = line.removeEmptyChars.trim
      parseNonLine(preprocessed) match {
        case Some(ParseStatus.CommentStart) =>
          parseLineWithPrevious(skipUntil(rest, ParseStatus.CommentEnd), previous, acc)

        case Some(ParseStatus.CommentEnd) =>
          parseLineWithPrevious(rest, previous, acc)

        case Some(_) =>
          effectOf((lineAndLineNums, acc).asRight)

        case None =>
          lineP.parse(preprocessed) match {
            case Right((remaining, SyncInfoAndLine(end, line))) =>
              previous match {
                case SyncInfoAndLine(start, previousLine) =>
                  parseLineWithPrevious(
                    rest,
                    SyncInfoAndLine(end, line),
                    acc :+ Smi.SmiLine(Smi.Start(start), Smi.End(end), Smi.Line(previousLine)),
                  )

                case SyncInfo(start) =>
                  parseLineWithPrevious(
                    rest,
                    SyncInfoAndLine(end, line),
                    acc,
                  )

                case SyncStartEndInfo(previousStart, previousEnd) =>
                  parseLineWithPrevious(
                    rest,
                    SyncInfoAndLine(end, line),
                    acc
                  )

                case SyncStartEndInfoAndLine(previousStart, previousEnd, previousLine) =>
                  parseLineWithPrevious(
                    rest,
                    SyncInfoAndLine(end, line),
                    acc :+ Smi.SmiLine(Smi.Start(previousStart), Smi.End(previousEnd), Smi.Line(previousLine)),
                  )
              }

            case Right((remaining, SyncStartEndInfoAndLine(newStart, newEnd, line))) =>
              previous match {
                case SyncInfoAndLine(start, previousLine) =>
                  parseLine(
                    rest,
                    acc :+
                      Smi.SmiLine(Smi.Start(start), Smi.End(newStart), Smi.Line(previousLine)) :+
                      Smi.SmiLine(Smi.Start(newStart), Smi.End(newEnd), Smi.Line(line)),
                  )

                case SyncInfo(start) =>
                  parseLine(
                    rest,
                    acc :+ Smi.SmiLine(Smi.Start(newStart), Smi.End(newEnd), Smi.Line(line)),
                  )

                case SyncStartEndInfo(previousStart, previousEnd) =>
                  parseLine(
                    rest,
                    acc :+ Smi.SmiLine(Smi.Start(newStart), Smi.End(newEnd), Smi.Line(line)),
                  )

                case SyncStartEndInfoAndLine(previousStart, previousEnd, previousLine) =>
                  parseLineWithPrevious(
                    rest,
                    SyncStartEndInfoAndLine(newStart, newEnd, line),
                    acc :+ Smi.SmiLine(Smi.Start(previousStart), Smi.End(previousEnd), Smi.Line(previousLine)),
                  )
              }

            case Right((remaining, SyncInfo(end))) =>
              previous match {
                case SyncInfoAndLine(start, previousLine) =>
                  parseLineWithPrevious(
                    rest,
                    SyncInfo(end),
                    acc :+ Smi.SmiLine(Smi.Start(start), Smi.End(end), Smi.Line(previousLine)),
                  )

                case SyncInfo(start) =>
                  parseLineWithPrevious(
                    rest,
                    SyncInfo(end),
                    acc,
                  )

                case SyncStartEndInfo(previousStart, previousEnd) =>
                  parseLineWithPrevious(
                    rest,
                    SyncStartEndInfo(previousStart, previousEnd),
                    acc,
                  )

                case SyncStartEndInfoAndLine(previousStart, previousEnd, previousLine) =>
                  parseLine(
                    rest,
                    acc :+ Smi.SmiLine(Smi.Start(previousStart), Smi.End(previousEnd), Smi.Line(previousLine)),
                  )
              }

            case Right((remaining, SyncStartEndInfo(newStart, newEnd))) =>
              previous match {
                case SyncInfoAndLine(start, previousLine) =>
                  parseLine(
                    rest,
                    acc :+ Smi.SmiLine(Smi.Start(start), Smi.End(newStart), Smi.Line(previousLine)),
                  )

                case SyncInfo(_) =>
                  parseLine(
                    rest,
                    acc,
                  )

                case SyncStartEndInfo(previousStart, previousEnd) =>
                  parseLine(
                    rest,
                    acc,
                  )

                case SyncStartEndInfoAndLine(previousStart, previousEnd, previousLine) =>
                  parseLineWithPrevious(
                    rest,
                    SyncStartEndInfo(newStart, newEnd),
                    acc :+ Smi.SmiLine(Smi.Start(previousStart), Smi.End(previousEnd), Smi.Line(previousLine)),
                  )
              }

            case Right((remaining, Line(line))) =>
              previous match {
                case SyncInfoAndLine(start, previousLine) =>
                  parseLineWithPrevious(
                    rest,
                    SyncInfoAndLine(start, s"$previousLine<br>$line"),
                    acc,
                  )

                case SyncInfo(start) =>
                  parseLineWithPrevious(
                    rest,
                    SyncInfoAndLine(start, line),
                    acc,
                  )

                case SyncStartEndInfo(previousStart, previousEnd) =>
                  parseLineWithPrevious(
                    rest,
                    SyncStartEndInfoAndLine(previousStart, previousEnd, line),
                    acc,
                  )

                case SyncStartEndInfoAndLine(previousStart, previousEnd, previousLine) =>
                  parseLineWithPrevious(
                    rest,
                    SyncStartEndInfoAndLine(previousStart, previousEnd, s"$previousLine<br>$line"),
                    acc,
                  )

              }

            case Left(err) =>
              effectOf(ParseError.SmiParseError(lineNum, line, err).asLeft)
          }
      }
    case Seq()                   =>
      effectOf((lineAndLineNums, acc).asRight)
  }

  private def parseNonLine(line: String): Option[ParseStatus] = {
    val lower = line.toLowerCase
    if (lower.startsWith(SmiStr.SamiStart))
      ParseStatus.SamiStart.some
    else if (lower.startsWith(SmiStr.SamiEnd))
      ParseStatus.SamiEnd.some
    else if (lower.startsWith(SmiStr.HeadStart))
      ParseStatus.HeadStart.some
    else if (lower.startsWith(SmiStr.HeadEnd))
      ParseStatus.HeadEnd.some
    else if (lower.startsWith(SmiStr.TitleStart))
      ParseStatus.TitleStart.some
    else if (lower.startsWith(SmiStr.TitleEnd))
      ParseStatus.TitleEnd.some
    else if (lower.startsWith(SmiStr.CommentStart) || lower.startsWith(SmiStr.CommentStart2)) {
      if lower.endsWith(SmiStr.CommentEnd) then ParseStatus.CommentEnd.some
      else ParseStatus.CommentStart.some
    } else if (lower.startsWith(SmiStr.CommentEnd))
      ParseStatus.CommentEnd.some
    else if (lower.startsWith(SmiStr.StyleStart))
      ParseStatus.StyleStart.some
    else if (lower.startsWith(SmiStr.StyleEnd))
      ParseStatus.StyleEnd.some
    else if (lower.startsWith(SmiStr.BodyStart))
      ParseStatus.BodyStart.some
    else if (lower.startsWith(SmiStr.BodyEnd))
      ParseStatus.BodyEnd.some
    else
      none
  }

  private def parseTitle[F[*]: Fx](
    lineAndLineNums: Seq[(String, Int)],
  ): F[(Option[Smi.Title], Seq[(String, Int)])] = {
    @tailrec
    def keepParsingTitle(
      lineAndLineNums: Seq[(String, Int)],
      acc: Vector[String],
    ): F[(Option[Smi.Title], Seq[(String, Int)])] =
      lineAndLineNums match {
        case (line, index) +: rest =>
          val preprocessed = line.removeEmptyChars.trim
          val lower        = preprocessed.toLowerCase
          val closeIndex   = lower.indexOf(SmiStr.TitleEnd)
          if (closeIndex >= 0) {
            val title = preprocessed.substring(0, closeIndex)
            if (title.isEmpty) {
              effectOf((Smi.Title(acc.mkString(" ")).some, (preprocessed.substring(closeIndex), index) +: rest))
            } else {
              effectOf(
                (Smi.Title((acc :+ title).mkString(" ")).some, (preprocessed.substring(closeIndex), index) +: rest),
              )
            }
          } else {
            keepParsingTitle(rest, acc :+ preprocessed)
          }
        case Seq()                 =>
          pureOf(none[Smi.Title], List.empty)
      }
    keepParsingTitle(lineAndLineNums, Vector.empty)
  }

}
