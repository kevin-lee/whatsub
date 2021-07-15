package whatsub

import Time.*

final case class Playtime(
  h: Playtime.Hours,
  m: Playtime.Minutes,
  s: Playtime.Seconds,
  ms: Playtime.Milliseconds,
) derives CanEqual {
  lazy val toMilliseconds: Long =
    (h.hours * HourSeconds + m.minutes * MinuteSeconds + s.seconds) * 1000 + ms.milliseconds
}
object Playtime    {

  def h(h: Int): Playtime = Playtime.fromMilliseconds(h * HourSeconds * 1000)

  def m(m: Int): Playtime = Playtime.fromMilliseconds(m * MinuteSeconds * 1000)

  def s(s: Int): Playtime = Playtime.fromMilliseconds(s * 1000)

  def ms(ms: Int): Playtime = Playtime.fromMilliseconds(ms)

  def fromMilliseconds(milliseconds: Long): Playtime = {
    val ms        = (milliseconds % 1000).toInt
    val inSeconds = (milliseconds / 1000).toInt
    val hours     = (inSeconds / HourSeconds).toInt

    val minutesLeftInSeconds = inSeconds - hours * HourSeconds

    val minutes = (minutesLeftInSeconds / MinuteSeconds).toInt
    val seconds = minutesLeftInSeconds - minutes * MinuteSeconds

    Playtime(
      Hours(hours),
      Minutes(minutes),
      Seconds(seconds),
      Milliseconds(ms),
    )
  }

  extension (playtime: Playtime) {

    def +(another: Playtime): Playtime =
      Playtime.fromMilliseconds(playtime.toMilliseconds + another.toMilliseconds)

    def -(another: Playtime): Playtime =
      Playtime.fromMilliseconds(playtime.toMilliseconds - another.toMilliseconds)

  }

  opaque type Hours = Int
  object Hours {
    def apply(hours: Int): Hours             = hours
    given hoursEqual: CanEqual[Hours, Hours] = CanEqual.derived
    extension (hours0: Hours) {
      def hours: Int = hours0
    }
  }

  opaque type Minutes = Int
  object Minutes {
    def apply(minutes: Int): Minutes = minutes

    given minutesEqual: CanEqual[Minutes, Minutes] = CanEqual.derived
    extension (minutes0: Minutes) {
      def minutes: Int = minutes0
    }
  }

  opaque type Seconds = Int
  object Seconds {
    def apply(seconds: Int): Seconds = seconds

    given secondsEqual: CanEqual[Seconds, Seconds] = CanEqual.derived
    extension (seconds0: Seconds) {
      def seconds: Int = seconds0
    }
  }

  opaque type Milliseconds = Int
  object Milliseconds {
    def apply(milliseconds: Int): Milliseconds = milliseconds

    given millisecondsEqual: CanEqual[Milliseconds, Milliseconds] = CanEqual.derived
    extension (milliseconds0: Milliseconds) {
      def milliseconds: Int = milliseconds0
    }
  }

}
