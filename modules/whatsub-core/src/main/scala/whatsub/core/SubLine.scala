package whatsub.core

import extras.render.Render

/** @author Kevin Lee
  * @since 2023-09-15
  */
final case class SubLine(
  index: SubLine.Index,
  start: SubLine.Start,
  end: SubLine.End,
  line: SubLine.Line
)
object SubLine {

  type Index = Index.Index
  object Index {
    opaque type Index = Int
    def apply(index: Int): Index = index

    given indexCanEqual: CanEqual[Index, Index] = CanEqual.derived

    given indexRender: Render[Index] = Render.fromToString

    extension (index: Index) {
      def value: Int = index
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
