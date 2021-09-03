package whatsub.parse

import whatsub.Playtime

/** @author Kevin Lee
  * @since 2021-08-21
  */
object SrtComponent {
  final case class Index(index: Int)
  final case class Playtimes(start: Playtime, end: Playtime)
  final case class Line(line: String)
}
