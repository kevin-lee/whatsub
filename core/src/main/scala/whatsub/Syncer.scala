package whatsub

/** @author Kevin Lee
  * @since 2021-07-09
  */
trait Syncer[A] {
  def sync(lines: List[A], sync: Syncer.Sync): List[A]
}
object Syncer   {

  def apply[A: Syncer]: Syncer[A] = summon[Syncer[A]]

  final case class Sync(direction: Direction, playtime: Playtime)
  enum Direction {
    case Forward
    case Backward
  }

  given syncer[A: CanShift]: Syncer[A] = (lines, sync) =>
    sync match {
      case Sync(direction, playtime) =>
        val shifter: A => A = direction match {
          case Syncer.Direction.Forward  =>
            line => CanShift[A].shiftForward(line, playtime)
          case Syncer.Direction.Backward =>
            line => CanShift[A].shiftBackward(line, playtime)
        }
        lines.map(shifter)
    }

}
