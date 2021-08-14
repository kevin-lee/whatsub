package whatsub

import FileF.FileError
import cats.Monad
import cats.data.EitherT
import cats.effect.kernel.MonadCancel
import cats.effect.{Resource, Sync}
import cats.syntax.all.*
import effectie.cats.*
import effectie.cats.Effectful.*
import effectie.cats.EitherTSupport.*
import pirate.{Command, ExitCode}
import piratex.{Help, Metavar}
import whatsub.WhatsubArgs.{ConvertArgs, SyncArgs}

import java.io.File
import scala.io.Source

/** @author Kevin Lee
  * @since 2021-06-30
  */
object Whatsub {

  private def parseAndConvert[F[_]: Monad: MCancel: Fx: CanCatch, A, B: CanRender](
    parser: String => F[Either[ParseError, A]],
    src: File,
    outFile: Option[ConvertArgs.OutFile],
  )(
    using convert: Convert[F, A, B],
  ): F[Either[WhatsubError, Unit]] =
    (for {
      srcSub <- Resource
                  .make(effectOf(Source.fromFile(src)))(source => effectOf(source.close()))
                  .use { srcSource =>
                    effectOf(srcSource.getLines.map(_.trim).mkString("\n"))
                      .flatMap(lines => parser(lines))
                  }
                  .eitherT
                  .leftMap(WhatsubError.ParseFailure(_))
      outSub <- convert
                  .convert(srcSub)
                  .eitherT
                  .leftMap(WhatsubError.ConversionFailure(_))
      _      <- effectOf(outFile)
                  .rightT[FileError]
                  .flatMapF(
                    _.fold(
                      effectOf(println(CanRender[B].render(outSub)).asRight),
                    )(out => FileF.fileF[F].writeFile(outSub, out.value)),
                  )
                  .leftMap {
                    case FileError.WriteFilure(file, throwable) =>
                      WhatsubError.FileWriteFailure(file, throwable)
                  }
    } yield ()).value

  def resync[F[_]: Monad: MCancel: Fx: CanCatch, A: CanRender](
    parser: String => F[Either[ParseError, A]],
    sync: Syncer.Sync,
    src: File,
    outFile: Option[SyncArgs.OutFile],
  )(using syncer: Syncer[F, A]): F[Either[WhatsubError, Unit]] = {
    (for {
      srcSub   <- Resource
                    .make(effectOf(Source.fromFile(src)))(source => effectOf(source.close()))
                    .use { srcSource =>
                      effectOf(srcSource.getLines.map(_.trim).mkString("\n"))
                        .flatMap(lines => parser(lines))
                    }
                    .eitherT
                    .leftMap(WhatsubError.ParseFailure(_))
      resynced <- syncer.sync(srcSub, sync).rightT
      _        <- effectOf(outFile)
                    .rightT[FileError]
                    .flatMapF(
                      _.fold(
                        effectOf(println(CanRender[A].render(resynced)).asRight),
                      )(out => FileF.fileF[F].writeFile(resynced, out.value)),
                    )
                    .leftMap {
                      case FileError.WriteFilure(file, throwable) =>
                        WhatsubError.FileWriteFailure(file, throwable)
                    }
    } yield ()).value
  }

  def apply[F[_]: Monad: MCancel: Fx: CanCatch](args: WhatsubArgs): F[Either[WhatsubError, Unit]] =
    args match {
      case ConvertArgs(
            ConvertArgs.From(SupportedSub.Smi),
            ConvertArgs.To(SupportedSub.Srt),
            srcFile,
            outFile,
          ) =>
        val src = srcFile.value.getCanonicalFile
        parseAndConvert[F, Smi, Srt](SmiParser.parse, src, outFile)

      case ConvertArgs(
            ConvertArgs.From(SupportedSub.Srt),
            ConvertArgs.To(SupportedSub.Smi),
            srcFile,
            outFile,
          ) =>
        val src = srcFile.value.getCanonicalFile
        parseAndConvert[F, Srt, Smi](SrtParser.parse, src, outFile)

      case ConvertArgs(ConvertArgs.From(SupportedSub.Smi), ConvertArgs.To(SupportedSub.Smi), _, _) =>
        pureOf(WhatsubError.NoConversion(SupportedSub.Smi).asLeft)

      case ConvertArgs(ConvertArgs.From(SupportedSub.Srt), ConvertArgs.To(SupportedSub.Srt), _, _) =>
        pureOf(WhatsubError.NoConversion(SupportedSub.Srt).asLeft)

      case SyncArgs(SyncArgs.Sub(SupportedSub.Smi), sync, srcFile, outFile) =>
        resync[F, Smi](SmiParser.parse, sync.value, srcFile.value, outFile)

      case SyncArgs(SyncArgs.Sub(SupportedSub.Srt), sync, srcFile, outFile) =>
        resync[F, Srt](SrtParser.parse, sync.value, srcFile.value, outFile)
    }
}
