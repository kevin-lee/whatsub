package whatsub.parse

import SubParsers.*
import cats.data.NonEmptyList
import cats.effect.*
import cats.parse.Rfc5234.*
import cats.parse.{Parser as P, Parser0 as P0, *}
import cats.syntax.all.*
import cats.{Functor, Monad}
import effectie.cats.*
import effectie.cats.Effectful.*
import effectie.cats.EitherTSupport.*
import whatsub.{Playtime, Srt}

import scala.collection.Iterator

/** @author Kevin Lee
  * @since 2021-07-03
  */
object SrtParser {

  val indexP: P[SrtComponent.Component] = (
    digit
      .rep
      .string <* P.end
  )
    .map(_.toInt)
    .map(index => SrtComponent.Index(index))

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

  val playtimeRangeP: P[SrtComponent.Component] =
    (playtimeP ~ ((spaceP.rep ~ arrowP ~ spaceP.rep) *> playtimeP) <* (spaceP.? ~ P.end))
      .map(SrtComponent.Playtimes.apply.tupled)

  val lineP: P[SrtComponent.Component] =
    (P.anyChar.rep.string <* P.end).map(line => SrtComponent.Line(line))

  val srtLineParser: P[SrtComponent.Component] = (indexP.backtrack | playtimeRangeP.backtrack | lineP)

  def parseAllWithIndexPlaytimesLines[F[_]: Fx: Monad](
    linesAndIndices: Seq[(String, Int)],
    srtIndex: SrtComponent.Index,
    playtimes: SrtComponent.Playtimes,
    lines: Vector[SrtComponent.Line],
    acc: Vector[Srt.SrtLine],
  ): F[Either[ParseError, Vector[Srt.SrtLine]]] = effectOf(linesAndIndices)
    .flatMap {
      case (line, index) +: rest =>
        effectOf(line.removeEmptyChars.trim)
          .flatMap { preprocessed =>
            if (preprocessed.isEmpty || preprocessed.forall(_.isWhitespace))
              parseAllWithIndexPlaytimesLines(rest, srtIndex, playtimes, lines, acc)
            else
              effectOf(srtLineParser.parse(preprocessed))
                .flatMap {
                  case Right((remaining, lineSrtIndex: SrtComponent.Index)) =>
                    parseAllWithIndex(
                      rest,
                      lineSrtIndex,
                      acc :+ Srt.SrtLine(
                        Srt.Index(srtIndex.index),
                        Srt.Start(playtimes.start.toMilliseconds),
                        Srt.End(playtimes.end.toMilliseconds),
                        Srt.Line(lines.map(_.line).mkString(System.lineSeparator())),
                      ),
                    )

                  case Right((remaining, linePlaytimes: SrtComponent.Playtimes)) =>
                    pureOf(
                      ParseError
                        .SrtParseInvalidLineError(
                          index,
                          preprocessed,
                          s"Index ($srtIndex), Playtimes ($playtimes) and lines (${lines.mkString("[", ",", "]")}) were already parsed so more line or end of SrtLine was expected but got playtimes again (linePlaytimes: $linePlaytimes)",
                        )
                        .asLeft,
                    )

                  case Right((remaining, line: SrtComponent.Line)) =>
                    parseAllWithIndexPlaytimesLines(rest, srtIndex, playtimes, lines :+ line, acc)

                  case Left(err) =>
                    pureOf(ParseError.SrtParseError(index, line, err).asLeft)

                }
          }

      case Seq() =>
        pureOf(
          (acc :+ Srt.SrtLine(
            Srt.Index(srtIndex.index),
            Srt.Start(playtimes.start.toMilliseconds),
            Srt.End(playtimes.end.toMilliseconds),
            Srt.Line(lines.map(_.line).mkString(System.lineSeparator())),
          )).asRight,
        )
    }

  def parseAllWithIndexPlaytimes[F[_]: Fx: Monad](
    linesAndIndices: Seq[(String, Int)],
    srtIndex: SrtComponent.Index,
    playtimes: SrtComponent.Playtimes,
    acc: Vector[Srt.SrtLine],
  ): F[Either[ParseError, Vector[Srt.SrtLine]]] = effectOf(linesAndIndices)
    .flatMap {
      case (line, index) +: rest =>
        effectOf(line.removeEmptyChars.trim)
          .flatMap { preprocessed =>
            if (preprocessed.isEmpty || preprocessed.forall(_.isWhitespace))
              parseAllWithIndexPlaytimes(rest, srtIndex, playtimes, acc)
            else
              effectOf(srtLineParser.parse(preprocessed))
                .flatMap {
                  case Right((remaining, lineSrtIndex: SrtComponent.Index)) =>
                    pureOf(
                      ParseError
                        .SrtParseInvalidLineError(
                          index,
                          preprocessed,
                          s"Index ($srtIndex) and Playtimes ($playtimes) were already parsed and line was expected but got index again (index: $lineSrtIndex)",
                        )
                        .asLeft,
                    )

                  case Right((remaining, linePlaytimes: SrtComponent.Playtimes)) =>
                    pureOf(
                      ParseError
                        .SrtParseInvalidLineError(
                          index,
                          preprocessed,
                          s"Index ($srtIndex) and Playtimes ($playtimes) were already parsed and line was expected but got playtimes again (linePlaytimes: $linePlaytimes)",
                        )
                        .asLeft,
                    )

                  case Right((remaining, srtLine: SrtComponent.Line)) =>
                    parseAllWithIndexPlaytimesLines(rest, srtIndex, playtimes, Vector(srtLine), acc)

                  case Left(err) =>
                    pureOf(ParseError.SrtParseError(index, line, err).asLeft)
                }
          }
      case Seq()                 =>
        pureOf(acc.asRight)
    }

  def parseAllWithIndex[F[_]: Fx: Monad](
    linesAndIndices: Seq[(String, Int)],
    srtIndex: SrtComponent.Index,
    acc: Vector[Srt.SrtLine],
  ): F[Either[ParseError, Vector[Srt.SrtLine]]] = effectOf(linesAndIndices)
    .flatMap {
      case (line, index) +: rest =>
        effectOf(line.removeEmptyChars.trim)
          .flatMap { preprocessed =>
            if (preprocessed.isEmpty || preprocessed.forall(_.isWhitespace))
              parseAllWithIndex(rest, srtIndex, acc)
            else
              effectOf(srtLineParser.parse(preprocessed))
                .flatMap {
                  case Right((remaining, lineSrtIndex: SrtComponent.Index)) =>
                    pureOf(
                      ParseError
                        .SrtParseInvalidLineError(
                          index,
                          preprocessed,
                          s"Index was already parsed ($srtIndex) and playtimes were expected but got index again (index: $lineSrtIndex)",
                        )
                        .asLeft,
                    )

                  case Right((remaining, playtimes: SrtComponent.Playtimes)) =>
                    parseAllWithIndexPlaytimes(rest, srtIndex, playtimes, acc)

                  case Right((remaining, line: SrtComponent.Line)) =>
                    pureOf(
                      ParseError
                        .SrtParseInvalidLineError(
                          index,
                          preprocessed,
                          s"Index was parsed ($srtIndex) and playtimes were expected but got line (line: $line)",
                        )
                        .asLeft,
                    )

                  case Left(err) =>
                    pureOf(ParseError.SrtParseError(index, line, err).asLeft)
                }
          }

      case Seq() =>
        pureOf(acc.asRight)
    }

  def parseAll[F[_]: Fx: Monad](
    linesAndIndices: Seq[(String, Int)],
    acc: Vector[Srt.SrtLine],
  ): F[Either[ParseError, Vector[Srt.SrtLine]]] = effectOf(linesAndIndices)
    .flatMap {
      case (line, index) +: rest =>
        effectOf(line.removeEmptyChars.trim)
          .flatMap { preprocessed =>
            if (preprocessed.isEmpty || preprocessed.forall(_.isWhitespace))
              parseAll(rest, acc)
            else
              effectOf(srtLineParser.parse(preprocessed))
                .flatMap {
                  case Right((remaining, index: SrtComponent.Index)) =>
                    parseAllWithIndex(rest, index, acc)

                  case Right((remaining, playtimes: SrtComponent.Playtimes)) =>
                    pureOf(
                      ParseError
                        .SrtParseInvalidLineError(
                          index,
                          preprocessed,
                          s"Index was expected but got Playtimes (playtimes: $playtimes)",
                        )
                        .asLeft,
                    )

                  case Right((remaining, line: SrtComponent.Line)) =>
                    pureOf(
                      ParseError
                        .SrtParseInvalidLineError(
                          index,
                          preprocessed,
                          s"Index was expected but got Srt line (line: $line)",
                        )
                        .asLeft,
                    )

                  case Left(err) =>
                    pureOf(ParseError.SrtParseError(index, line, err).asLeft)
                }
          }
      case Seq()                 =>
        pureOf(acc.asRight)
    }

  def parse[F[_]: Fx: Monad](lines: Seq[String]): F[Either[ParseError, Srt]] =
    effectOf(lines.zipWithIndex)
      .rightT[ParseError]
      .flatMapF {
        parseAll(_, Vector.empty)
      }
      .map(lines => Srt.apply(lines.toList))
      .value

}
