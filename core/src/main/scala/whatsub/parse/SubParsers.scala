package whatsub.parse

import cats.parse.Rfc5234.*

/** @author Kevin Lee
  * @since 2021-08-15
  */
object SubParsers {

  val spaceP   = wsp
  val newlineP = (crlf | cr | lf)

  extension (s: String) {
    def removeEmptyChars: String = s.replaceAll("[\uFEFF-\uFFFF]", "")
  }

}
