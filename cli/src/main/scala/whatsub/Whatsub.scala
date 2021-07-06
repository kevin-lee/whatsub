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

  private def parseAndConvert[F[_]: Sync, A, B: CanRender](
    parser: String => Either[ParseError, A],
    src: File,
    outFile: Option[ConvertArgs.OutFile],
  )(
    using convert: Convert[A, B],
  ): F[Either[WhatsubError, Unit]] =
    (for {
      srcSub <- EitherT(
                  Resource
                    .make(Sync[F].delay(Source.fromFile(src)))(source => Sync[F].delay(source.close()))
                    .use { srcSource =>
                      Sync[F]
                        .delay(srcSource.getLines.map(_.trim).mkString("\n"))
                        .flatMap(lines => Sync[F].delay(parser(lines)))
                    },
                ).leftMap(WhatsubError.ParseFailure(_))
      outSub <- EitherT(Sync[F].delay(convert.convert(srcSub)))
                  .leftMap(WhatsubError.ConversionFailure(_))
      _      <- EitherT
                  .right(Sync[F].delay(outFile))
                  .flatMapF(
                    _.fold(
                      Sync[F].delay(println(CanRender[B].render(outSub)).asRight),
                    )(out => FileF.fileF[F].writeFile(outSub, out.outFile)),
                  )
                  .leftMap {
                    case FileError.WriteFilure(file, throwable) =>
                      WhatsubError.FileWriteFailure(file, throwable)
                  }
    } yield ()).value

  def apply[F[_]: Sync](args: WhatsubArgs): F[Either[WhatsubError, Unit]] = args match {
    case ConvertArgs(
          ConvertArgs.From(SupportedSub.Smi),
          ConvertArgs.To(SupportedSub.Srt),
          srcFile,
          outFile,
        ) =>
      val src = srcFile.srcFile.getCanonicalFile
      parseAndConvert[F, Smi, Srt](SmiParser.parse, src, outFile)

    case ConvertArgs(
          ConvertArgs.From(SupportedSub.Srt),
          ConvertArgs.To(SupportedSub.Smi),
          srcFile,
          outFile,
        ) =>
      val src = srcFile.srcFile.getCanonicalFile
      parseAndConvert[F, Srt, Smi](SrtParser.parse, src, outFile)

    case ConvertArgs(ConvertArgs.From(SupportedSub.Smi), ConvertArgs.To(SupportedSub.Smi), _, _) =>
      Sync[F].pure(WhatsubError.NoConversion(SupportedSub.Smi).asLeft)

    case ConvertArgs(ConvertArgs.From(SupportedSub.Srt), ConvertArgs.To(SupportedSub.Srt), _, _) =>
      Sync[F].pure(WhatsubError.NoConversion(SupportedSub.Srt).asLeft)
  }
}
