package whatsub

import FileF.FileError
import cats.data.EitherT
import cats.effect.{Resource, Sync}
import cats.syntax.all.*
import pirate.{Command, ExitCode}
import piratex.{Help, Metavar}
import whatsub.WhatsubArgs.ConvertArgs

import java.io.File
import scala.io.Source

/** @author Kevin Lee
  * @since 2021-06-30
  */
object Whatsub {

  def apply[F[_]: Sync](args: WhatsubArgs): F[Either[WhatsubError, Unit]] = args match {
    case ConvertArgs(
          ConvertArgs.From(SupportedSub.Smi),
          ConvertArgs.To(SupportedSub.Srt),
          srcFile,
          outFile,
        ) =>
      val src = srcFile.srcFile.getCanonicalFile
      (for {
        smi <- EitherT(
                 Resource
                   .make(Sync[F].delay(Source.fromFile(src)))(source => Sync[F].delay(source.close()))
                   .use { srcSource =>
                     Sync[F]
                       .delay(srcSource.getLines.map(_.trim).mkString("\n"))
                       .flatMap(lines => Sync[F].delay(SmiParser.parse(lines)))
                   },
               ).leftMap(WhatsubError.ParseFailure(_))
        srt <- EitherT(Sync[F].delay(Convert[Smi, Srt].convert(smi)))
                 .leftMap(WhatsubError.ConversionFailure(_))
        _   <- EitherT
                 .right(Sync[F].pure(outFile))
                 .flatMapF(
                   _.fold(
                     Sync[F].delay(println(srt.render).asRight),
                   )(out =>
                     FileF
                       .fileF[F]
                       .writeFile(srt, out.outFile),
                   ),
                 )
                 .leftMap {
                   case FileError.WriteFilure(file, throwable) =>
                     WhatsubError.FileWriteFailure(file, throwable)
                 }
      } yield ()).value

    case ConvertArgs(
          ConvertArgs.From(SupportedSub.Srt),
          ConvertArgs.To(SupportedSub.Smi),
          srcFile,
          outFile,
        ) =>
      val src = srcFile.srcFile.getCanonicalFile
      (for {
        srt <- EitherT(
                 Resource
                   .make(Sync[F].delay(Source.fromFile(src)))(source => Sync[F].delay(source.close()))
                   .use { srcSource =>
                     Sync[F]
                       .delay(srcSource.getLines.map(_.trim).mkString("\n"))
                       .flatMap(lines => Sync[F].delay(SrtParser.parse(lines)))
                   },
               ).leftMap(WhatsubError.ParseFailure(_))
        smi <- EitherT(Sync[F].delay(Convert[Srt, Smi].convert(srt)))
                 .leftMap(WhatsubError.ConversionFailure(_))
        _   <-
          EitherT
            .right(Sync[F].pure(outFile))
            .flatMapF(
              _.fold(
                Sync[F].delay(println(smi.render).asRight),
              )(out =>
                FileF
                  .fileF[F]
                  .writeFile(smi, out.outFile),
              ),
            )
            .leftMap {
              case FileError.WriteFilure(file, throwable) =>
                WhatsubError.FileWriteFailure(file, throwable)
            }
      } yield ()).value

    case ConvertArgs(ConvertArgs.From(SupportedSub.Smi), ConvertArgs.To(SupportedSub.Smi), _, _) =>
      Sync[F].pure(WhatsubError.NoConversion(SupportedSub.Smi).asLeft)

    case ConvertArgs(ConvertArgs.From(SupportedSub.Srt), ConvertArgs.To(SupportedSub.Srt), _, _) =>
      Sync[F].pure(WhatsubError.NoConversion(SupportedSub.Srt).asLeft)
  }
}
