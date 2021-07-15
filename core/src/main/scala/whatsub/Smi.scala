package whatsub

import cats.syntax.all.*

final case class Smi(
  title: Smi.Title,
  lines: List[Smi.SmiLine],
) derives CanEqual
object Smi {

  given canRenderSmi: CanRender[Smi] = _.render

  given smiSync: Syncer[Smi] =
    (sub, sync) =>
      sync match {
        case Syncer.Sync(Syncer.Direction.Forward, playtime)  =>
          sub.copy(lines = sub.lines.map(_ + playtime))
        case Syncer.Sync(Syncer.Direction.Backward, playtime) =>
          sub.copy(lines = sub.lines.map(_ - playtime))
      }

  extension (smi: Smi) {
    def render: String =
      s"""<SAMI>
         |<HEAD>
         |  <TITLE>${smi.title.title}</TITLE>
         |</HEAD>
         |<BODY>
         |""".stripMargin + (
        smi
          .lines
          .map { strLine =>
            s"""  <SYNC Start=${strLine.start.start}><P>${strLine.line.line}
               |  <SYNC Start=${strLine.end.end}><P>&nbsp;
               |""".stripMargin
          }
          .mkString
      ) +
        s"""</BODY>
           |</SAMI>
           |""".stripMargin

    def sync(sync: Syncer.Sync)(using Syncer[SmiLine]): Smi =
      Syncer[Smi].sync(smi, sync)

  }

  final case class SmiLine(
    start: Smi.Start,
    end: Smi.End,
    line: Smi.Line,
  ) derives CanEqual

  object SmiLine {
    extension (smiLine: SmiLine) {
      def +(playtime: Playtime): SmiLine = {
        val milliseconds = playtime.toMilliseconds
        smiLine.copy(
          start = Start(smiLine.start.start + milliseconds),
          end = End(smiLine.end.end + milliseconds),
        )
      }

      def -(playtime: Playtime): SmiLine = {
        val milliseconds = playtime.toMilliseconds
        smiLine.copy(
          start = Start(smiLine.start.start - milliseconds),
          end = End(smiLine.end.end - milliseconds),
        )
      }
    }
  }

  opaque type Title = String
  object Title {
    def apply(title: String): Title = title

    given titleCanEqual: CanEqual[Title, Title] = CanEqual.derived
    extension (title0: Title) {
      def title: String = title0
    }
  }

  opaque type Start = Long
  object Start {
    def apply(start: Long): Start = start

    given startCanEqual: CanEqual[Start, Start] = CanEqual.derived
    extension (start0: Start) {
      def start: Long = start0
    }
  }

  opaque type End = Long
  object End {
    def apply(end: Long): End = end

    given endCanEqual: CanEqual[End, End] = CanEqual.derived
    extension (end0: End) {
      def end: Long = end0
    }
  }

  opaque type Line = String
  object Line {
    def apply(line: String): Line = line

    given lineCanEqual: CanEqual[Line, Line] = CanEqual.derived
    extension (line0: Line) {
      def line: String = line0
    }
  }

}
