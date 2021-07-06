package whatsub

/** @author Kevin Lee
  * @since 2021-07-06
  */
trait CanRender[A] {
  def render(a: A): String
}

object CanRender {
  given apply[A: CanRender]: CanRender[A] = summon[CanRender[A]]
}
