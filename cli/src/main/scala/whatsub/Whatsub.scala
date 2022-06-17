package whatsub

import FileF.FileError
import cats.data.{EitherT, NonEmptyList}
import cats.effect.kernel.MonadCancel
import cats.effect.{Resource, Sync}
import cats.syntax.all.*
import cats.{Monad, Monoid}
import effectie.cats.console.given
import effectie.core.*
import effectie.syntax.all.*
import extras.cats.syntax.all.*
import extras.scala.io.syntax.color.*
import pirate.{Command, ExitCode}
import piratex.{Help, Metavar}
import whatsub.WhatsubArgs.{CharsetArgs, ConvertArgs, SyncArgs}
import whatsub.charset.{Charset, ConvertCharset}
import whatsub.convert.Convert
import whatsub.parse.{ParseError, SmiParser, SrtParser}
import whatsub.sync.Syncer
import whatsub.typeclasses.Scala3TypeClasses.*

import java.io.{BufferedWriter, File, FileWriter, Writer}
import java.nio.charset.Charset as JCharset
import scala.io.{Codec, Source}

/** @author Kevin Lee
  * @since 2021-06-30
  */
object Whatsub {

  private def parseAndConvert[F[*]: Monad: MCancel: Fx, A, B: CanRender](
    parser: Seq[String] => F[Either[ParseError, A]],
    src: File,
    outFile: Option[ConvertArgs.OutFile],
  )(
    using convert: Convert[F, A, B],
  ): F[Either[WhatsubError, Unit]] =
    if (outFile.exists(_.value.getCanonicalPath === src.getCanonicalPath)) {
      pureOf(WhatsubError.IdenticalSrcAndOut(src, outFile.map(_.value)).asLeft[Unit])
    } else {
      (for {
        srcSub <- Resource
                    .make(effectOf(Source.fromFile(src)(Codec.UTF8)))(source => effectOf(source.close()))
                    .use { srcSource =>
                      effectOf(srcSource.getLines.to(LazyList))
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
                    .flatMap(
                      _.fold(
                        putStrLn(outSub.render).rightT[FileError],
                      )(out => FileF[F].writeFile(outSub, out.value).eitherT),
                    )
                    .leftMap {
                      case FileError.WriteFailure(file, throwable) =>
                        WhatsubError.FileWriteFailure(file, throwable)
                    }
      } yield ()).value
    }

  def resync[F[*]: Monad: MCancel: Fx, A: CanRender](
    parser: Seq[String] => F[Either[ParseError, A]],
    sync: Syncer.Sync,
    src: File,
    outFile: Option[SyncArgs.OutFile],
  )(using syncer: Syncer[F, A]): F[Either[WhatsubError, Unit]] =
    if (outFile.exists(_.value.getCanonicalPath === src.getCanonicalPath)) {
      pureOf(WhatsubError.IdenticalSrcAndOut(src, outFile.map(_.value)).asLeft[Unit])
    } else {
      (for {
        srcSub   <- Resource
                      .make(effectOf(Source.fromFile(src)(Codec.UTF8)))(source => effectOf(source.close()))
                      .use { srcSource =>
                        effectOf(srcSource.getLines.to(LazyList))
                          .flatMap(lines => parser(lines))
                      }
                      .eitherT
                      .leftMap(WhatsubError.ParseFailure(_))
        resynced <- srcSub.sync(sync).rightT
        _        <- outFile
                      .fold(
                        putStrLn(CanRender[A].render(resynced)).rightT[FileError],
                      )(out => FileF[F].writeFile(resynced, out.value).eitherT)
                      .leftMap {
                        case FileError.WriteFailure(file, throwable) =>
                          WhatsubError.FileWriteFailure(file, throwable)
                      }
      } yield ()).value
    }

  def charsetListAll[F[*]: Monad: Fx]: F[Unit] = {
    import scala.jdk.CollectionConverters.*
    putStr(
      JCharset
        .availableCharsets()
        .asScala
        .keys
        .mkString("== List of available charsets ==\n", "\n", "\n"),
    )
  }

  def charsetConvert[F[*]: Monad: MCancel: Fx](
    from: ConvertCharset.From,
    to: ConvertCharset.To,
    src: CharsetArgs.SrcFile,
    out: Option[CharsetArgs.OutFile],
  ): F[Either[WhatsubError, Unit]] = {

    given monoidUnit: Monoid[Unit] with {
      override def empty: Unit = ()

      override def combine(x: Unit, y: Unit): Unit = ()
    }

    if (out.exists(_.value.getCanonicalPath === src.value.getCanonicalPath)) {
      pureOf(WhatsubError.IdenticalSrcAndOut(src.value, out.map(_.value)).asLeft[Unit])
    } else {
      out match {
        case None =>
          val EoL = System.lineSeparator
          ConvertCharset
            .convertFileCharset[F, Unit]
            .convert(from, to)(src.value)(s => putStr(s + EoL))
            .eitherT
            .leftMap(WhatsubError.CharsetConversion(_))
            .value

        case Some(outFile) =>
          Resource
            .make[F, Writer](effectOf(new BufferedWriter(new FileWriter(outFile.value))))(writer =>
              effectOf(writer.close()),
            )
            .use { writer =>
              val EoL                  = System.lineSeparator
              val f: String => F[Unit] = s => effectOf(writer.write(s + EoL))
              (
                ConvertCharset
                  .convertFileCharset[F, Unit]
                  .convert(from, to)(src.value)(f)
                  .eitherT
                  .leftMap(WhatsubError.CharsetConversion(_)) *>
                  putStrLn[F] {
                    val fromFile = from.render.magenta.bold
                    val toFile   = to.render.magenta.bold
                    s""">> [${"Success".green}] Charset conversion from $fromFile to $toFile
                       |>> The converted subtitle file has been written at
                       |>>   ${outFile.value.toString.blue.bold}
                       |""".stripMargin
                  }.rightT
              ).value
            }

      }
    }
  }

  def apply[F[*]: Monad: MCancel: Fx](args: WhatsubArgs): F[Either[WhatsubError, Unit]] =
    args match {
      case ConvertArgs(
            Some(ConvertArgs.From(SupportedSub.Smi)),
            Some(ConvertArgs.To(SupportedSub.Srt)),
            srcFile,
            outFile,
          ) =>
        val src = srcFile.value.getCanonicalFile
        parseAndConvert[F, Smi, Srt](SmiParser.parse, src, outFile)

      case ConvertArgs(
            Some(ConvertArgs.From(SupportedSub.Srt)),
            Some(ConvertArgs.To(SupportedSub.Smi)),
            srcFile,
            outFile,
          ) =>
        val src = srcFile.value.getCanonicalFile
        parseAndConvert[F, Srt, Smi](SrtParser.parse, src, outFile)

      case ConvertArgs(Some(ConvertArgs.From(SupportedSub.Smi)), Some(ConvertArgs.To(SupportedSub.Smi)), _, _) =>
        pureOf(WhatsubError.NoConversion(SupportedSub.Smi).asLeft)

      case ConvertArgs(Some(ConvertArgs.From(SupportedSub.Srt)), Some(ConvertArgs.To(SupportedSub.Srt)), _, _) =>
        pureOf(WhatsubError.NoConversion(SupportedSub.Srt).asLeft)

      case ConvertArgs(
            None,
            None,
            srcFile,
            outFile,
          ) =>
        pureOf(
          WhatsubError
            .MissingSubTypes(
              NonEmptyList.of("from" -> srcFile.value.some, "to" -> outFile.map(_.value)),
            )
            .asLeft,
        )

      case ConvertArgs(
            None,
            _,
            srcFile,
            _,
          ) =>
        pureOf(
          WhatsubError
            .MissingSubTypes(
              NonEmptyList.of("from" -> srcFile.value.some),
            )
            .asLeft,
        )

      case ConvertArgs(
            _,
            None,
            _,
            outFile,
          ) =>
        pureOf(
          WhatsubError
            .MissingSubTypes(
              NonEmptyList.of("to" -> outFile.map(_.value)),
            )
            .asLeft,
        )

      case SyncArgs(Some(SyncArgs.Sub(SupportedSub.Smi)), sync, srcFile, outFile) =>
        resync[F, Smi](SmiParser.parse, sync.value, srcFile.value, outFile)

      case SyncArgs(Some(SyncArgs.Sub(SupportedSub.Srt)), sync, srcFile, outFile) =>
        resync[F, Srt](SrtParser.parse, sync.value, srcFile.value, outFile)

      case SyncArgs(None, _, srcFile, _) =>
        pureOf(WhatsubError.MissingSubTypes(NonEmptyList.of("sub" -> srcFile.value.some)).asLeft)

      case CharsetArgs(CharsetArgs.CharsetTask.ListAll) =>
        charsetListAll[F].map(_.asRight[WhatsubError])

      case CharsetArgs(CharsetArgs.CharsetTask.Convert(CharsetArgs.From(from), CharsetArgs.To(to), srcFile, outFile)) =>
        charsetConvert[F](ConvertCharset.From(Charset(from)), ConvertCharset.To(Charset(to)), srcFile, outFile)
    }

}
