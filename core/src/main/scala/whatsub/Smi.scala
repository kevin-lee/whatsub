package whatsub

import cats.Monad
import cats.syntax.all.*
import effectie.cats.Effectful.*
import effectie.cats.Fx

final case class Smi(
  title: Smi.Title,
  lines: List[Smi.SmiLine],
) derives CanEqual
object Smi {

  given canRenderSmi: CanRender[Smi] = _.render

  given smiSync[F[_]: Fx: Monad]: Syncer[F, Smi] =
    (sub, sync) =>
      for {
        shift <- pureOf(sync match {
                   case Syncer.Sync(Syncer.Direction.Forward, playtime)  =>
                     ((_: SmiLine) + playtime)
                   case Syncer.Sync(Syncer.Direction.Backward, playtime) =>
                     ((_: SmiLine) - playtime)
                 })
        lines <- effectOf(sub.lines.map(shift))
      } yield sub.copy(lines = lines)

  extension (smi: Smi) {
    def render: String =
      s"""<SAMI>
         |<HEAD>
         |  <TITLE>${smi.title.value}</TITLE>
         |</HEAD>
         |<BODY>
         |""".stripMargin + (
        smi
          .lines
          .map { strLine =>
            s"""  <SYNC Start=${strLine.start.value}><P>${strLine.line.value}
               |  <SYNC Start=${strLine.end.value}><P>&nbsp;
               |""".stripMargin
          }
          .mkString
      ) +
        s"""</BODY>
           |</SAMI>
           |""".stripMargin

    def sync[F[_]: Fx: Monad](sync: Syncer.Sync): F[Smi] =
      Syncer[F, Smi].sync(smi, sync)

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
          start = Start(smiLine.start.value + milliseconds),
          end = End(smiLine.end.value + milliseconds),
        )
      }

      def -(playtime: Playtime): SmiLine = {
        val milliseconds = playtime.toMilliseconds
        smiLine.copy(
          start = Start(smiLine.start.value - milliseconds),
          end = End(smiLine.end.value - milliseconds),
        )
      }
    }
  }

  type Title = Title.Title
  object Title {
    opaque type Title = String
    def apply(title: String): Title = title

    given titleCanEqual: CanEqual[Title, Title] = CanEqual.derived
    extension (title: Title) {
      def value: String = title
    }
  }

  type Start = Start.Start
  object Start {
    opaque type Start = Long
    def apply(start: Long): Start = start

    given startCanEqual: CanEqual[Start, Start] = CanEqual.derived
    extension (start: Start) {
      def value: Long = start
    }
  }

  type End = End.End
  object End {
    opaque type End = Long
    def apply(end: Long): End = end

    given endCanEqual: CanEqual[End, End] = CanEqual.derived
    extension (end: End) {
      def value: Long = end
    }
  }

  type Line = Line.Line
  object Line {
    opaque type Line = String
    def apply(line: String): Line = line

    given lineCanEqual: CanEqual[Line, Line] = CanEqual.derived
    extension (line: Line) {
      def value: String = line
    }
  }

}
