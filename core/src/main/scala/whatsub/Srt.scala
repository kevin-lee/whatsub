package whatsub

import Time.*
import cats.syntax.all.*

final case class Srt(lines: List[Srt.SrtLine]) derives CanEqual
object Srt {

  final case class SrtLine(
    index: Srt.Index,
    start: Srt.Start,
    end: Srt.End,
    line: Srt.Line
  ) derives CanEqual

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

  private def formatTwoDigitBasedNumber(n: Int): String = f"$n%02d"
  private def formatThreeDigitBasedNumber(n: Int): String = f"$n%03d"

  def millisecondsToSrtTime(milliseconds: Long): String = {
    val ms        = (milliseconds % 1000).toInt
    val inSeconds = (milliseconds / 1000).toInt
    val hours     = (inSeconds / HourSeconds).toInt

    val minutesLeftInSeconds = inSeconds - hours * HourSeconds

    val minutes = (minutesLeftInSeconds / MinuteSeconds).toInt
    val seconds = minutesLeftInSeconds - minutes * MinuteSeconds
    s"${formatTwoDigitBasedNumber(hours)}:${formatTwoDigitBasedNumber(minutes)}:${formatTwoDigitBasedNumber(seconds)},${formatThreeDigitBasedNumber(ms)}"
  }

  extension (srt: Srt) {
    def render: String = srt
      .lines
      .map {
        case Srt.SrtLine(index, start, end, line) =>
          s"""${index.index}
             |${millisecondsToSrtTime(start)} --> ${millisecondsToSrtTime(end)}
             |$line
             |""".stripMargin
      }
      .mkString("\n")
  }

}
