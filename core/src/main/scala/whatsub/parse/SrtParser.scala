package whatsub.parse

import SubParsers.*
import cats.data.NonEmptyList
import cats.effect.*
import cats.parse.Rfc5234.*
import cats.parse.{Parser as P, Parser0 as P0, *}
import cats.syntax.all.*
import cats.{Functor, Monad}
import effectie.core.*
import effectie.syntax.all.*
import extras.cats.syntax.all.*
import whatsub.{Playtime, Srt}

import scala.collection.Iterator

/** @author Kevin Lee
  * @since 2021-07-03
  */
object SrtParser {

  val indexP: P[SrtComponent.Index] = (
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

  val playtimeRangeP: P[SrtComponent.Playtimes] =
    (playtimeP ~ ((spaceP.rep ~ arrowP ~ spaceP.rep) *> playtimeP) <* (spaceP.? ~ P.end))
      .map(SrtComponent.Playtimes.apply.tupled)

  val lineP: P[SrtComponent.Line] =
    (P.anyChar.rep.string <* P.end).map(line => SrtComponent.Line(line))

//  val srtLineParser: P[SrtComponent.Component] = (indexP.backtrack | playtimeRangeP.backtrack | lineP)
  val srtIndexParser: P[SrtComponent.Index]        = indexP
  val srtPlaytimeParser: P[SrtComponent.Playtimes] = playtimeRangeP
  val srtLineParser: P[SrtComponent.Line]          = lineP

  def parseAllWithIndexPlaytimesLines[F[*]: Fx: Monad](
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
              parseAll(
                rest,
                acc :+ Srt.SrtLine(
                  Srt.Index(srtIndex.index),
                  Srt.Start(playtimes.start.toMilliseconds),
                  Srt.End(playtimes.end.toMilliseconds),
                  Srt.Line(lines.map(_.line).mkString(System.lineSeparator())),
                ),
              )
            else
              effectOf(srtLineParser.parse(preprocessed))
                .flatMap {
                  case Right((remaining, line: SrtComponent.Line)) =>
                    parseAllWithIndexPlaytimesLines(rest, srtIndex, playtimes, lines :+ line, acc)

                  case Left(err) =>
                    pureOf(
                      ParseError
                        .SrtParseError(
                          index,
                          line,
                          (
                            s"Index ($srtIndex), Playtimes ($playtimes) and lines (${lines.mkString("[", ",", "]")}) were already parsed " +
                              s"so only more line or end of SrtLine was expected but got something else"
                          ).some,
                          err,
                        )
                        .asLeft,
                    )

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

  def parseAllWithIndexPlaytimes[F[*]: Fx: Monad](
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
                  case Right((remaining, srtLine: SrtComponent.Line)) =>
                    parseAllWithIndexPlaytimesLines(rest, srtIndex, playtimes, Vector(srtLine), acc)

                  case Left(err) =>
                    pureOf(
                      ParseError
                        .SrtParseError(
                          index,
                          line,
                          (
                            s"Index ($srtIndex) and Playtimes ($playtimes) were already parsed and line was expected " +
                              "but got something else"
                          ).some,
                          err,
                        )
                        .asLeft,
                    )
                }
          }
      case Seq()                 =>
        pureOf(acc.asRight)
    }

  def parseAllWithIndex[F[*]: Fx: Monad](
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
              effectOf(srtPlaytimeParser.parse(preprocessed))
                .flatMap {
                  case Right((remaining, playtimes: SrtComponent.Playtimes)) =>
                    parseAllWithIndexPlaytimes(rest, srtIndex, playtimes, acc)

                  case Left(err) =>
                    pureOf(
                      ParseError
                        .SrtParseError(
                          index,
                          line,
                          s"Index was already parsed ($srtIndex) and playtimes were expected but got something else".some,
                          err,
                        )
                        .asLeft,
                    )
                }
          }

      case Seq() =>
        pureOf(acc.asRight)
    }

  def parseAll[F[*]: Fx: Monad](
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
              effectOf(srtIndexParser.parse(preprocessed))
                .flatMap {
                  case Right((remaining, index: SrtComponent.Index)) =>
                    parseAllWithIndex(rest, index, acc)

                  case Left(err) =>
                    pureOf(
                      ParseError
                        .SrtParseError(
                          index,
                          line,
                          s"Index was expected but got something ele".some,
                          err,
                        )
                        .asLeft,
                    )
                }
          }
      case Seq()                 =>
        pureOf(acc.asRight)
    }

  def parse[F[*]: Fx: Monad](lines: Seq[String]): F[Either[ParseError, Srt]] =
    effectOf(lines.map(_.removeEmptyChars.trim).zipWithIndex)
      .rightT[ParseError]
      .flatMapF {
        parseAll(_, Vector.empty)
      }
      .map(lines => Srt.apply(lines.toList))
      .value

}
