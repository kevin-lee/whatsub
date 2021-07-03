package whatsub

import cats.parse.Rfc5234.*

object SubParsers {

  val spaceP      = wsp
  val newlineP    = (crlf | cr | lf)

  extension (s: String) {
    def removeEmptyChars: String = s.replaceAll("[\uFEFF-\uFFFF]", "")
  }

}
