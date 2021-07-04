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
    case Smi => "Smi"
    case Srt => "Srt"
  }

  extension (supportedSub: SupportedSub) {
    def render: String = supportedSub match {
      case SupportedSub.Smi => "SAMI (smi)"
      case SupportedSub.Srt => "SubRip (srt)"
    }
  }

}
