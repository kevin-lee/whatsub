package whatsub

import WhatsubArgs.*
import cats.syntax.option.*
import pirate.Read
import whatsub.sync.Syncer

import java.io.File
import java.nio.charset.{Charset, UnsupportedCharsetException}
import scala.util.Try
import scala.util.control.NonFatal

import whatsub.typeclasses.CanBeString.given

enum WhatsubArgs derives CanEqual {
  case ConvertArgs(
    from: Option[ConvertArgs.From],
    to: Option[ConvertArgs.To],
    src: ConvertArgs.SrcFile,
    out: Option[ConvertArgs.OutFile],
  )
  case SyncArgs(
    sub: Option[SyncArgs.Sub],
    sync: SyncArgs.Sync,
    src: SyncArgs.SrcFile,
    out: Option[SyncArgs.OutFile],
  )
  case CharsetArgs(
    charsetTask: CharsetArgs.CharsetTask,
  )
}
object WhatsubArgs {

  def supportedSubFromString(supportedSub: String): Option[SupportedSub] = supportedSub match {
    case "smi" | "SMI" => SupportedSub.Smi.some
    case "srt" | "SRT" => SupportedSub.Srt.some
    case _ => none[SupportedSub]
  }

  object ConvertArgs {
    type From = From.From
    object From {
      opaque type From = SupportedSub
      def apply(from: SupportedSub): From = from

      def unapply(from: From): Some[SupportedSub] = Some(from.value)

      given fromCanEqual: CanEqual[From, From] = CanEqual.derived

      given fromRead: Read[From] = Read.eitherRead { supportedSub =>
        import scalaz.*
        import Scalaz.*
        supportedSubFromString(supportedSub)
          .map(From(_))
          .toRightDisjunction("Unknown subtitle type for 'from'. 'from' should be either smi or srt")
      }

      extension (from: From) {
        def value: SupportedSub = from
      }
    }

    type To = To.To
    object To {
      opaque type To = SupportedSub
      def apply(to: SupportedSub): To = to

      def unapply(to: To): Some[SupportedSub] = Some(to.value)

      given toCanEqual: CanEqual[To, To] = CanEqual.derived

      given toRead: Read[To] = Read.eitherRead { supportedSub =>
        import scalaz.*
        import Scalaz.*
        supportedSubFromString(supportedSub)
          .map(To(_))
          .toRightDisjunction("Unknown subtitle type for 'to'. 'to' should be either smi or srt")
      }

      extension (to: To) {
        def value: SupportedSub = to
      }
    }

    type SrcFile = SrcFile.SrcFile
    object SrcFile {
      opaque type SrcFile = File
      def apply(srcFile: File): SrcFile = srcFile

      given srcFileCanEqual: CanEqual[SrcFile, SrcFile] = CanEqual.derived

      extension (srcFile: SrcFile) {
        def value: File      = srcFile
        def filename: String = srcFile.getName.stringValue
      }

    }

    type OutFile = OutFile.OutFile
    object OutFile {
      opaque type OutFile = File
      def apply(outFile: File): OutFile = outFile

      given outFileCanEqual: CanEqual[OutFile, OutFile] = CanEqual.derived

      extension (outFile: OutFile) {
        def value: File      = outFile
        def filename: String = outFile.getName.stringValue
      }

    }

  }

  object SyncArgs {

    type Sub = Sub.Sub
    object Sub {
      opaque type Sub = SupportedSub
      def apply(sub: SupportedSub): Sub = sub

      def unapply(sub: Sub): Some[SupportedSub] = Some(sub.value)

      given subCanEqual: CanEqual[Sub, Sub] = CanEqual.derived

      given subRead: Read[Sub] = Read.eitherRead { supportedSub =>
        import scalaz.*
        import Scalaz.*
        supportedSubFromString(supportedSub)
          .map(Sub(_))
          .toRightDisjunction("Unknown subtitle type for 'sub'. 'sub' should be either smi or srt")
      }

      extension (sub: Sub) {
        def value: SupportedSub = sub
      }
    }

    type Sync = Sync.Sync
    object Sync {
      opaque type Sync = Syncer.Sync
      def apply(sync: Syncer.Sync): Sync = sync

      given syncCanEqual: CanEqual[Sync, Sync] = CanEqual.derived

      given syncRead: Read[Sync] = Read.eitherRead { syncArg =>
//        import cats.parse.{Parser as P, Parser0 as P0, *}
        import cats.parse.{Parser as P, *}
        import cats.parse.Rfc5234.*
        import cats.syntax.either.*
        scalaz
          .\/
          .fromEither(
            ((P.char('+').map(_ => Syncer.Direction.Forward).backtrack | P
              .char('-')
              .map(_ => Syncer.Direction.Backward))
              .parse(syncArg))
              .leftMap(_ => "Invalid direction. It should be either + or -")
              .flatMap {
                case (remaining, direction) =>
                  ((
                    (digit.rep.string <* P.string("h")).backtrack.? ~
                      (digit.rep.string <* P.char('m')).backtrack.? ~
                      (digit.rep.string <* P.string("s")).backtrack.? ~
                      (digit.rep.string).?
                  ).map {
                    case ((((None, None), None), None)) =>
                      "No sync playtime info found".asLeft[Playtime]

                    case ((((h, m), s), ms)) =>
                      (
                        Playtime.h(h.map(_.toInt).getOrElse(0)) +
                          Playtime.m(m.map(_.toInt).getOrElse(0)) +
                          Playtime.s(s.map(_.toInt).getOrElse(0)) +
                          Playtime.ms(ms.map(_.toInt).getOrElse(0)),
                      ).asRight[String]

                  }).parse(remaining)
                    .leftMap(_ =>
                      "No valid playtime info found. It should be {hours}h{minutes}m{seconds}s{milliseconds}.",
                    )
                    .flatMap {
                      case (_, Right(playtime)) => Syncer.Sync(direction, playtime).asRight
                      case (_, Left(err)) => err.asLeft
                    }
              },
          )

      }

      extension (sync: Sync) {
        def value: Syncer.Sync = sync
      }
    }

    type SrcFile = SrcFile.SrcFile
    object SrcFile {
      opaque type SrcFile = File
      def apply(srcFile: File): SrcFile = srcFile

      given srcFileCanEqual: CanEqual[SrcFile, SrcFile] = CanEqual.derived

      extension (srcFile: SrcFile) {
        def value: File      = srcFile
        def filename: String = srcFile.getName.stringValue
      }

    }

    type OutFile = OutFile.OutFile
    object OutFile {
      opaque type OutFile = File
      def apply(outFile: File): OutFile = outFile

      given outFileCanEqual: CanEqual[OutFile, OutFile] = CanEqual.derived

      extension (outFile: OutFile) {
        def value: File      = outFile
        def filename: String = outFile.getName.stringValue
      }

    }

  }

  object CharsetArgs {
    enum CharsetTask derives CanEqual {
      case ListAll
      case Convert(
        from: From,
        to: To,
        srcFile: SrcFile,
        outFile: Option[OutFile],
      )
    }

    private type ParamName = ParamName.ParamName
    private object ParamName {
      opaque type ParamName = String
      def apply(paramName: String): ParamName = paramName

      given paramNameCanEqual: CanEqual[ParamName, ParamName] = CanEqual.derived

      extension (paramName: ParamName) {
        def value: String = paramName
      }
    }

    import scalaz.*
    import Scalaz.*

    def charsetFromString(charset: String, paramName: ParamName): String \/ Charset =
      Try(Charset.forName(charset))
        .toDisjunction
        .map(_.nn)
        .leftMap {
          case ex: UnsupportedCharsetException =>
            s"${ex.getCharsetName} is unknown charset for '$paramName'. To see all supported ones please run 'charset list' command"
          case NonFatal(ex) =>
            s"$charset is unknown charset for '$paramName'. To see all supported ones please run 'charset list' command"
        }

    type From = From.From
    object From {
      opaque type From = Charset
      def apply(from: Charset): From = from

      given fromCanEqual: CanEqual[From, From] = CanEqual.derived

      def unapply(from: From): Some[Charset] = Some(from.value)

      given fromRead: Read[From] = Read.eitherRead { fromCharset =>
//        import scalaz.*
//        import Scalaz.*
        charsetFromString(fromCharset, ParamName("from")).map(From(_))
      }

      extension (from: From) {
        def value: Charset = from
      }
    }

    type To = To.To
    object To {
      opaque type To = Charset
      def apply(to: Charset): To = to

      given toCanEqual: CanEqual[To, To] = CanEqual.derived

      def unapply(to: To): Some[Charset] = Some(to.value)

      given fromRead: Read[To] = Read.eitherRead { toCharset =>
//        import scalaz.*
//        import Scalaz.*
        charsetFromString(toCharset, ParamName("to")).map(To(_))
      }

      extension (to: To) {
        def value: Charset = to
      }
    }

    type SrcFile = SrcFile.SrcFile
    object SrcFile {
      opaque type SrcFile = File
      def apply(srcFile: File): SrcFile = srcFile

      given srcFileCanEqual: CanEqual[SrcFile, SrcFile] = CanEqual.derived

      extension (srcFile: SrcFile) {
        def value: File      = srcFile
        def filename: String = srcFile.getName.stringValue
      }

    }

    type OutFile = OutFile.OutFile
    object OutFile {
      opaque type OutFile = File
      def apply(outFile: File): OutFile = outFile

      given outFileCanEqual: CanEqual[OutFile, OutFile] = CanEqual.derived

      extension (outFile: OutFile) {
        def value: File      = outFile
        def filename: String = outFile.getName.stringValue
      }

    }

  }

}
