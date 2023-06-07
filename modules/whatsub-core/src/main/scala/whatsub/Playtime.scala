package whatsub

import Time.*

import scala.annotation.targetName

final case class Playtime(
  h: Playtime.Hours,
  m: Playtime.Minutes,
  s: Playtime.Seconds,
  ms: Playtime.Milliseconds,
) derives CanEqual {
  lazy val toMilliseconds: Long =
    (h.value * HourSeconds + m.value * MinuteSeconds + s.value) * 1000 + ms.value
}
object Playtime {

  def h(h: Int): Playtime = Playtime.fromMilliseconds(h * HourSeconds * 1000)

  def m(m: Int): Playtime = Playtime.fromMilliseconds(m * MinuteSeconds * 1000)

  def s(s: Int): Playtime = Playtime.fromMilliseconds(s * 1000)

  def ms(ms: Int): Playtime = Playtime.fromMilliseconds(ms)

  def fromMilliseconds(milliseconds: Long): Playtime = {
    val ms        = (milliseconds % 1000).toInt
    val inSeconds = (milliseconds / 1000).toInt
    val hours     = inSeconds / HourSeconds

    val minutesLeftInSeconds = inSeconds - hours * HourSeconds

    val minutes = minutesLeftInSeconds / MinuteSeconds
    val seconds = minutesLeftInSeconds - minutes * MinuteSeconds

    Playtime(
      Hours(hours),
      Minutes(minutes),
      Seconds(seconds),
      Milliseconds(ms),
    )
  }

  extension (playtime: Playtime) {

    @targetName("plus")
    def +(another: Playtime): Playtime =
      Playtime.fromMilliseconds(playtime.toMilliseconds + another.toMilliseconds)

    @targetName("minus")
    def -(another: Playtime): Playtime =
      Playtime.fromMilliseconds(playtime.toMilliseconds - another.toMilliseconds)

  }

  type Hours = Hours.Hours
  object Hours {
    opaque type Hours = Int
    def apply(hours: Int): Hours = hours

    given hoursEqual: CanEqual[Hours, Hours] = CanEqual.derived
    extension (hours: Hours) {
      def value: Int = hours
    }
  }

  type Minutes = Minutes.Minutes
  object Minutes {
    opaque type Minutes = Int
    def apply(minutes: Int): Minutes = minutes

    given minutesEqual: CanEqual[Minutes, Minutes] = CanEqual.derived
    extension (minutes: Minutes) {
      def value: Int = minutes
    }
  }

  type Seconds = Seconds.Seconds
  object Seconds {
    opaque type Seconds = Int
    def apply(seconds: Int): Seconds = seconds

    given secondsEqual: CanEqual[Seconds, Seconds] = CanEqual.derived
    extension (seconds: Seconds) {
      def value: Int = seconds
    }
  }

  type Milliseconds = Milliseconds.Milliseconds
  object Milliseconds {
    opaque type Milliseconds = Int
    def apply(milliseconds: Int): Milliseconds = milliseconds

    given millisecondsEqual: CanEqual[Milliseconds, Milliseconds] = CanEqual.derived
    extension (milliseconds: Milliseconds) {
      def value: Int = milliseconds
    }
  }

}
