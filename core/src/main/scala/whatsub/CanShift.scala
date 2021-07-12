package whatsub

/** @author Kevin Lee
  * @since 2021-07-09
  */
trait CanShift[A] {
  def shiftForward(a: A, playtime: Playtime): A
  def shiftBackward(a: A, playtime: Playtime): A
}

object CanShift {
  def apply[A: CanShift]: CanShift[A] = summon[CanShift[A]]
}
