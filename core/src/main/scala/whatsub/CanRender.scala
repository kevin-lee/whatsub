package whatsub

/** @author Kevin Lee
  * @since 2021-07-06
  */
trait CanRender[A] {
  extension (a: A) {
    def render: String
  }
}

object CanRender {
  def apply[A: CanRender]: CanRender[A] = summon[CanRender[A]]
}
