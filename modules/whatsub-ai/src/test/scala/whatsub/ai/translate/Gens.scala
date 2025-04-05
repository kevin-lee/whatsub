package whatsub.ai.translate

import hedgehog.*
import whatsub.core.SubLine

import scala.concurrent.duration.*

/** @author Kevin Lee
  * @since 2023-09-16
  */
object Gens {
  def genSubLinesAndExpected: Gen[(List[SubLine], List[SubLine])] =
    for {
      sources  <- for {
                    s1 <- Gen.string(Gen.alphaNum, Range.linear(1, 30)).list(Range.linear(1, 5_000))
                    s2 <- Gen.string(Gen.alphaNum, Range.linear(1, 30)).list(Range.linear(1, 5_000))
                    s3 <- Gen.string(Gen.alphaNum, Range.linear(1, 30)).list(Range.linear(1, 5_000))
                    s4 <- Gen.string(Gen.alphaNum, Range.linear(1, 30)).list(Range.linear(1, 5_000))
                    s5 <- Gen.string(Gen.alphaNum, Range.linear(1, 30)).list(Range.linear(1, 5_000))
                  } yield s1 ++ s2 ++ s3 ++ s4 ++ s5
      expected <- {
        val length    = sources.length
        val howMany   = length / 5
        val remainder = length % 5
        for {
          s1 <- Gen.string(Gen.alphaNum, Range.linear(1, 30)).list(Range.singleton(howMany))
          s2 <- Gen.string(Gen.alphaNum, Range.linear(1, 30)).list(Range.singleton(howMany))
          s3 <- Gen.string(Gen.alphaNum, Range.linear(1, 30)).list(Range.singleton(howMany))
          s4 <- Gen.string(Gen.alphaNum, Range.linear(1, 30)).list(Range.singleton(howMany))
          s5 <- Gen.string(Gen.alphaNum, Range.linear(1, 30)).list(Range.singleton(howMany + remainder))
        } yield s1 ++ s2 ++ s3 ++ s4 ++ s5
      }
      timecodeRanges = genTimecodeRanges(2.hours, sources.length)
    } yield {
      val sourceLines = sources
        .zip(timecodeRanges)
        .zipWithIndex
        .map {
          case ((line, (start, end)), index) =>
            SubLine(SubLine.Index(index), start, end, SubLine.Line(line))
        }

      val expecetedLines = sourceLines.zip(expected).map {
        case (SubLine(index, start, end, _), expectedLine) =>
          SubLine(index, start, end, SubLine.Line(expectedLine))
      }
      (sourceLines, expecetedLines)
    }

  def genTimecodeRanges(howLong: FiniteDuration, howMany: Int): List[(SubLine.Start, SubLine.End)] = {
    val howLongInMillis = howLong.toMillis
    val range           = howLongInMillis / howMany

    (0L to howLongInMillis by range).map { n =>
      genTimecodeRange(n, range)
    }.toList
  }

  def genTimecodeRange(n: Long, range: Long): (SubLine.Start, SubLine.End) = {
    val start = scala.util.Random.between(n, n + (range / 2))
    val end   = scala.util.Random.between(n + (range / 2), n + range)
    (SubLine.Start(start), SubLine.End(end))
  }

}
