package whatsub

import cats.Show

/** @author Kevin Lee
  * @since 2021-07-04
  */
enum SupportedSub {
  case Smi
  case Srt
}
object SupportedSub {

  given supportedSubShow: Show[SupportedSub] = {
    case Smi => "SAMI (smi)"
    case Srt => "SubRip (srt)"
  }

}
