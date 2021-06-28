package whatsub

final case class Smi(
  title: Smi.Title,
  lines: List[Smi.SmiLine]
) derives CanEqual

object Smi {

  final case class SmiLine(
    start: Smi.Start,
    end: Smi.End,
    line: Smi.Line
  ) derives CanEqual

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
