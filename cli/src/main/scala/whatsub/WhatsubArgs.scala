package whatsub

import WhatsubArgs.*
import cats.syntax.option.*
import pirate.Read

import java.io.File

enum WhatsubArgs derives CanEqual {
  case ConvertArgs(
    from: ConvertArgs.From,
    to: ConvertArgs.To,
    src: ConvertArgs.SrcFile,
    out: Option[ConvertArgs.OutFile],
  )
}
object WhatsubArgs {

  def supportedSubFromString(supportedSub: String): Option[SupportedSub] = supportedSub match {
    case "smi" | "SMI" => SupportedSub.Smi.some
    case "srt" | "SRT" => SupportedSub.Srt.some
    case _             => none[SupportedSub]
  }

  object ConvertArgs {
    opaque type From = SupportedSub
    object From {
      def apply(from: SupportedSub): From = from

      def unapply(from: From): Some[SupportedSub] = Some(from.from)

      given fromCanEqual: CanEqual[From, From] = CanEqual.derived

      implicit final val fromRead: Read[From] = Read.eitherRead { supportedSub =>
        import scalaz.*
        import Scalaz.*
        supportedSubFromString(supportedSub)
          .map(From(_))
          .toRightDisjunction("Unknown subtitle type for 'from'. 'from' should be either smi or srt")
      }

      extension (from0: From) {
        def from: SupportedSub = from0
      }
    }

    opaque type To = SupportedSub
    object To {
      def apply(to: SupportedSub): To = to

      def unapply(to: To): Some[SupportedSub] = Some(to.to)

      given toCanEqual: CanEqual[To, To] = CanEqual.derived

      implicit final val toRead: Read[To] = Read.eitherRead { supportedSub =>
        import scalaz.*
        import Scalaz.*
        supportedSubFromString(supportedSub)
          .map(To(_))
          .toRightDisjunction("Unknown subtitle type for 'to'. 'to' should be either smi or srt")
      }

      extension (to0: To) {
        def to: SupportedSub = to0
      }
    }

    opaque type SrcFile = File
    object SrcFile {
      def apply(srcFile: File): SrcFile = srcFile

      given srcFileCanEqual: CanEqual[SrcFile, SrcFile] = CanEqual.derived

      extension (srcFile0: SrcFile) {
        def srcFile: File = srcFile0
      }

    }

    opaque type OutFile = File
    object OutFile {
      def apply(outFile: File): OutFile = outFile

      given outFileCanEqual: CanEqual[OutFile, OutFile] = CanEqual.derived

      extension (outFile0: OutFile) {
        def outFile: File = outFile0
      }

    }

  }

}
