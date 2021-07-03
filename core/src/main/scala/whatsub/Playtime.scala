package whatsub

import Time.*

final case class Playtime(
  h: Playtime.Hours,
  m: Playtime.Minutes,
  s: Playtime.Seconds,
  ms: Playtime.Milliseconds,
) derives CanEqual
object Playtime {

  extension (playtime: Playtime) {

    def +(another: Playtime): Playtime = (playtime, another) match {
      case (Playtime(h1, m1, s1, ms1), Playtime(h2, m2, s2, ms2)) =>
        Playtime(h1 + h2, m1 + m2, s1 + s2, ms1 + ms2)
    }

    def -(another: Playtime): Playtime = (playtime, another) match {
      case (Playtime(h1, m1, s1, ms1), Playtime(h2, m2, s2, ms2)) =>
        Playtime(h1 - h2, m1 - m2, s1 - s2, ms1 - ms2)
    }

    def toMilliseconds: Long = playtime match {
      case Playtime(h, m, s, ms) =>
        (h.hours * HourSeconds + m.minutes * MinuteSeconds + s.seconds) * 1000 + ms.milliseconds
    }

  }

  opaque type Hours = Int
  object Hours {
    def apply(hours: Int): Hours             = hours
    given hoursEqual: CanEqual[Hours, Hours] = CanEqual.derived
    extension (hours0: Hours) {
      def hours: Int               = hours0
      def +(another: Hours): Hours = hours0.hours + another.hours
      def -(another: Hours): Hours = hours0.hours - another.hours
    }
  }

  opaque type Minutes = Int
  object Minutes {
    def apply(minutes: Int): Minutes = minutes

    given minutesEqual: CanEqual[Minutes, Minutes] = CanEqual.derived
    extension (minutes0: Minutes) {
      def minutes: Int                 = minutes0
      def +(another: Minutes): Minutes = minutes0.minutes + another.minutes
      def -(another: Minutes): Minutes = minutes0.minutes - another.minutes
    }
  }

  opaque type Seconds = Int
  object Seconds {
    def apply(seconds: Int): Seconds = seconds

    given secondsEqual: CanEqual[Seconds, Seconds] = CanEqual.derived
    extension (seconds0: Seconds) {
      def seconds: Int                 = seconds0
      def +(another: Seconds): Seconds = seconds0.seconds + another.seconds
      def -(another: Seconds): Seconds = seconds0.seconds - another.seconds
    }
  }

  opaque type Milliseconds = Int
  object Milliseconds {
    def apply(milliseconds: Int): Milliseconds = milliseconds

    given millisecondsEqual: CanEqual[Milliseconds, Milliseconds] = CanEqual.derived
    extension (milliseconds0: Milliseconds) {
      def milliseconds: Int                      = milliseconds0
      def +(another: Milliseconds): Milliseconds = milliseconds0.milliseconds + another.milliseconds
      def -(another: Milliseconds): Milliseconds = milliseconds0.milliseconds - another.milliseconds
    }
  }

}
