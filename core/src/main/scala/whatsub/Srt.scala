package whatsub

import Time.*
import cats.Monad
import cats.syntax.all.*
import effectie.cats.Effectful.*
import effectie.cats.Fx
import whatsub.sync.Syncer

final case class Srt(
  lines: List[Srt.SrtLine],
) derives CanEqual
object Srt {

  private def formatTwoDigitBasedNumber(n: Int): String   = f"$n%02d"
  private def formatThreeDigitBasedNumber(n: Int): String = f"$n%03d"

  def renderMillisecondsToSrtTime(milliseconds: Long): String =
    Playtime.fromMilliseconds(milliseconds) match {
      case Playtime(hours, minutes, seconds, ms) =>
        val hh  = formatTwoDigitBasedNumber(hours.value)
        val mm  = formatTwoDigitBasedNumber(minutes.value)
        val ss  = formatTwoDigitBasedNumber(seconds.value)
        val mss = formatThreeDigitBasedNumber(ms.value)
        s"$hh:$mm:$ss,$mss"
    }

  given canRenderSrt: CanRender[Srt] with {
    extension (srt: Srt) {
      def render: String = srt
        .lines
        .map {
          case Srt.SrtLine(index, start, end, line) =>
            s"""${index.value}
               |${renderMillisecondsToSrtTime(start.value)} --> ${renderMillisecondsToSrtTime(end.value)}
               |$line
               |""".stripMargin
        }
        .mkString("\n")
    }
  }

  given srtSync[F[*]: Fx: Monad]: Syncer[F, Srt] with {
    extension (sub: Srt) {
      def sync(sync: Syncer.Sync): F[Srt] =
        for {
          shift <- pureOf(sync match {
                     case Syncer.Sync(Syncer.Direction.Forward, playtime)  =>
                       ((_: SrtLine) + playtime)
                     case Syncer.Sync(Syncer.Direction.Backward, playtime) =>
                       ((_: SrtLine) - playtime)
                   })
          lines <- effectOf(sub.lines.map(shift))
        } yield sub.copy(lines = lines)
    }
  }

  final case class SrtLine(
    index: Srt.Index,
    start: Srt.Start,
    end: Srt.End,
    line: Srt.Line,
  ) derives CanEqual

  object SrtLine {

    extension (srtLine: SrtLine) {
      def +(playtime: Playtime): SrtLine = {
        val milliseconds = playtime.toMilliseconds
        srtLine.copy(
          start = Start(srtLine.start.value + milliseconds),
          end = End(srtLine.end.value + milliseconds),
        )
      }
      def -(playtime: Playtime): SrtLine = {
        val milliseconds = playtime.toMilliseconds
        srtLine.copy(
          start = Start(srtLine.start.value - milliseconds),
          end = End(srtLine.end.value - milliseconds),
        )
      }
    }

  }

  type Index = Index.Index
  object Index {
    opaque type Index = Int
    def apply(index: Int): Index = index
    extension (index: Index) {
      def value: Int = index
    }
  }

  type Start = Start.Start
  object Start {
    opaque type Start = Long
    def apply(start: Long): Start = start
    extension (start: Start) {
      def value: Long = start
    }
  }

  type End = End.End
  object End {
    opaque type End = Long
    def apply(start: Long): End = start
    extension (end: End) {
      def value: Long = end
    }
  }

  type Line = Line.Line
  object Line {
    opaque type Line = String
    def apply(line: String): Line = line
    extension (line: Line) {
      def value: String = line
    }
  }

}
