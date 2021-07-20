package whatsub

/** @author Kevin Lee
  * @since 2021-06-23
  */
enum SmiComponent {
  case SamiStart
  case Head(title: SmiComponent.Title)
  case BodyStart
  case BodyLine(
    start: SmiComponent.Milliseconds,
    end: SmiComponent.Milliseconds,
    line: SmiComponent.Line,
  )
  case BodyEnd
  case SamiEnd
}

object SmiComponent {
  type Title = Title.Title
  object Title {
    opaque type Title = String
    def apply(title: String): Title = title

    given titleCanEqual: CanEqual[Title, Title] = CanEqual.derived
    extension (title: Title) {
      def value: String = title
    }
  }

  type Milliseconds = Milliseconds.Milliseconds
  object Milliseconds {
    opaque type Milliseconds = Long
    def apply(milliseconds: Long): Milliseconds = milliseconds

    given millisecondsCanEqual: CanEqual[Milliseconds, Milliseconds] = CanEqual.derived
    extension (milliseconds: Milliseconds) {
      def value: Long = milliseconds
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
