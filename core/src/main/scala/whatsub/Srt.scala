package whatsub

import Time.*
import cats.syntax.all.*

final case class Srt(
  lines: List[Srt.SrtLine],
) derives CanEqual
object Srt {

  private def formatTwoDigitBasedNumber(n: Int): String   = f"$n%02d"
  private def formatThreeDigitBasedNumber(n: Int): String = f"$n%03d"

  def renderMillisecondsToSrtTime(milliseconds: Long): String =
    Playtime.fromMilliseconds(milliseconds) match {
      case Playtime(hours, minutes, seconds, ms) =>
        val hh  = formatTwoDigitBasedNumber(hours.hours)
        val mm  = formatTwoDigitBasedNumber(minutes.minutes)
        val ss  = formatTwoDigitBasedNumber(seconds.seconds)
        val mss = formatThreeDigitBasedNumber(ms.milliseconds)
        s"$hh:$mm:$ss,$mss"
    }

  extension (srt: Srt) {
    def render: String = srt
      .lines
      .map {
        case Srt.SrtLine(index, start, end, line) =>
          s"""${index.index}
             |${renderMillisecondsToSrtTime(start)} --> ${renderMillisecondsToSrtTime(end)}
             |$line
             |""".stripMargin
      }
      .mkString("\n")

    def sync(sync: Syncer.Sync)(using Syncer[SrtLine]): Srt =
      Syncer[Srt].sync(srt, sync)

  }

  given canRenderSrt: CanRender[Srt] = _.render

  given smiSync: Syncer[Srt] =
    (sub, sync) =>
      sync match {
        case Syncer.Sync(Syncer.Direction.Forward, playtime)  =>
          sub.copy(lines = sub.lines.map(_ + playtime))
        case Syncer.Sync(Syncer.Direction.Backward, playtime) =>
          sub.copy(lines = sub.lines.map(_ - playtime))
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
          start = Start(srtLine.start.start + milliseconds),
          end = End(srtLine.end.end + milliseconds),
        )
      }
      def -(playtime: Playtime): SrtLine = {
        val milliseconds = playtime.toMilliseconds
        srtLine.copy(
          start = Start(srtLine.start.start - milliseconds),
          end = End(srtLine.end.end - milliseconds),
        )
      }
    }

  }

  opaque type Index = Int
  object Index {
    def apply(index: Int): Index = index
    extension (index0: Index) {
      def index: Int = index0
    }
  }

  opaque type Start = Long
  object Start {
    def apply(start: Long): Start = start
    extension (start0: Start) {
      def start: Long = start0
    }
  }

  opaque type End = Long
  object End {
    def apply(start: Long): End = start
    extension (end0: End) {
      def end: Long = end0
    }
  }

  opaque type Line = String
  object Line {
    def apply(line: String): Line = line
    extension (line0: Line) {
      def line: String = line0
    }
  }

}
