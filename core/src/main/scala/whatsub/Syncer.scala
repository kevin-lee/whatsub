package whatsub

/** @author Kevin Lee
  * @since 2021-07-09
  */
trait Syncer[A] {
  def sync(a: A, sync: Syncer.Sync): A
}
object Syncer   {

  def apply[A: Syncer]: Syncer[A] = summon[Syncer[A]]

  final case class Sync(direction: Direction, playtime: Playtime) derives CanEqual
  enum Direction {
    case Forward
    case Backward
  }

}
