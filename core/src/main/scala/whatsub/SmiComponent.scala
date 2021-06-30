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
    line: SmiComponent.Line
  )
  case BodyEnd
  case SamiEnd
}

object SmiComponent {
  opaque type Title = String
  object Title {
    def apply(title: String): Title = title

    given titleCanEqual: CanEqual[Title, Title] = CanEqual.derived
    extension (title0: Title) {
      def title: String = title0
    }
  }

  opaque type Milliseconds = Long
  object Milliseconds {
    def apply(milliseconds: Long): Milliseconds = milliseconds

    given millisecondsCanEqual: CanEqual[Milliseconds, Milliseconds] = CanEqual.derived
    extension (milliseconds0: Milliseconds) {
      def milliseconds: Long = milliseconds0
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
