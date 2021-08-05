package whatsub

/** @author Kevin Lee
  * @since 2021-07-09
  */
trait Syncer[F[_], A] {
  def sync(a: A, sync: Syncer.Sync): F[A]
}

object Syncer {

  def apply[F[_], A](using Syncer[F, A]): Syncer[F, A] = summon[Syncer[F, A]]

  final case class Sync(direction: Direction, playtime: Playtime) derives CanEqual
  enum Direction {
    case Forward
    case Backward
  }

}