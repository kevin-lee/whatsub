package whatsub

import extras.scala.io.syntax.color.*
import scalaz.*
import Scalaz.*
import pirate.*
import Pirate.*
import WhatsubArgs.*
import cats.Show
import pirate.internal.ParseTraversal
import whatsub.charset.Charset
import whatsub.info.WhatsubBuildInfo

import java.io.File
import scala.io

import whatsub.typeclasses.Scala3TypeClasses.*
import whatsub.typeclasses.CanBeString.given

/** @author Kevin Lee
  * @since 2021-07-01
  */
object WhatsubArgsParser {

  def fromParse: Parse[Option[ConvertArgs.From]] = flag[ConvertArgs.From](
    both('f', "from"),
    metavar("<from>") |+| description(
      "A type of subtitle to be converted from. Optional. " +
        "If missing, it gets the from type from the extension of the src file.",
    ),
  ).option

  def toParse: Parse[Option[ConvertArgs.To]] = flag[ConvertArgs.To](
    both('t', "to"),
    metavar("<to>") |+| description(
      "A type of subtitle to be converted to. Optional. " +
        "If missing it gets the to type from the extension of the out file.",
    ),
  ).option

  def srcFileParse: Parse[ConvertArgs.SrcFile] = argument[File](
    metavar("<src>") |+| description("The source subtitle file"),
  ).map(ConvertArgs.SrcFile(_))

  def outFileParse: Parse[Option[ConvertArgs.OutFile]] = argument[File](
    metavar("<out>") |+| description(
      s"""An ${"optional".green} output subtitle file. If missing, the result is printed out.""",
    ),
  ).option.map(_.map(ConvertArgs.OutFile(_)))

  def convertParse: Parse[WhatsubArgs] = (WhatsubArgs.ConvertArgs.apply |*| (
    fromParse,
    toParse,
    srcFileParse,
    outFileParse,
  )).map {
    case WhatsubArgs.ConvertArgs(
          None,
          to,
          srcFile,
          outFile,
        ) if srcFile.filename.endsWith(".smi") =>
      WhatsubArgs.ConvertArgs(ConvertArgs.From(SupportedSub.Smi).some, to, srcFile, outFile)

    case WhatsubArgs.ConvertArgs(
          None,
          to,
          srcFile,
          outFile,
        ) if srcFile.filename.endsWith(".srt") =>
      WhatsubArgs.ConvertArgs(ConvertArgs.From(SupportedSub.Srt).some, to, srcFile, outFile)

    case arg @ WhatsubArgs.ConvertArgs(
          None,
          to,
          srcFile,
          outFile,
        ) =>
      val firstLine = FileF.firstLineFromFile(srcFile.value)
      firstLine match {
        case Some(line) =>
          val trimmed = line.trim.nn
          if trimmed.equalsIgnoreCase("<SAMI>") then
            WhatsubArgs.ConvertArgs(ConvertArgs.From(SupportedSub.Smi).some, to, srcFile, outFile)
          else if trimmed.equalsIgnoreCase("1") then
            WhatsubArgs.ConvertArgs(ConvertArgs.From(SupportedSub.Srt).some, to, srcFile, outFile)
          else arg

        case None =>
          arg
      }

    case whatsubArgs =>
      whatsubArgs
  }.map {
    case WhatsubArgs.ConvertArgs(
          from,
          None,
          srcFile,
          Some(outFile),
        ) if outFile.filename.endsWith(".smi") =>
      WhatsubArgs.ConvertArgs(from, ConvertArgs.To(SupportedSub.Smi).some, srcFile, outFile.some)

    case WhatsubArgs.ConvertArgs(
          from,
          None,
          srcFile,
          Some(outFile),
        ) if outFile.filename.endsWith(".srt") =>
      WhatsubArgs.ConvertArgs(from, ConvertArgs.To(SupportedSub.Srt).some, srcFile, outFile.some)

    case whatsubArgs =>
      whatsubArgs

  }

  def subParse: Parse[Option[SyncArgs.Sub]] = flag[SyncArgs.Sub](
    both('t', "sub-type"),
    metavar("<sub-type>") |+| description(
      s"""A type of subtitle. Either ${"smi".blue} or ${"srt".blue}. """ +
        "Optional. If missing, it gets the sub-type from the extension of the src file.",
    ),
  ).option

  def syncParse: Parse[SyncArgs.Sync] = flag[SyncArgs.Sync](
    both('m', "sync"),
    metavar("<sync>") |+| description(
      s"""resync playtime (e.g. shift ${"1 h".blue}our ${"12 m".blue}inutes ${"3 s".blue}econds ${"100".blue} milliseconds forward: ${"+1h12m3s100".blue}""",
    ),
  )

  def syncSrcFileParse: Parse[SyncArgs.SrcFile] = argument[File](
    metavar("<src>") |+| description("The source subtitle file"),
  ).map(SyncArgs.SrcFile(_))

  def syncOutFileParse: Parse[Option[SyncArgs.OutFile]] = argument[File](
    metavar("<out>") |+| description(
      s"""An ${"optional".green} output subtitle file. If missing, the result is printed out.""",
    ),
  ).option.map(_.map(SyncArgs.OutFile(_)))

  def syncArgsParse: Parse[WhatsubArgs] = (SyncArgs.apply |*| (
    subParse,
    syncParse,
    syncSrcFileParse,
    syncOutFileParse,
  )).map {
    case WhatsubArgs.SyncArgs(
          None,
          sync,
          srcFile,
          out,
        ) if srcFile.filename.endsWith(".smi") =>
      WhatsubArgs.SyncArgs(
        SyncArgs.Sub(SupportedSub.Smi).some,
        sync,
        srcFile,
        out,
      )

    case WhatsubArgs.SyncArgs(
          None,
          sync,
          srcFile,
          out,
        ) if srcFile.filename.endsWith(".srt") =>
      WhatsubArgs.SyncArgs(
        SyncArgs.Sub(SupportedSub.Srt).some,
        sync,
        srcFile,
        out,
      )

    case arg @ WhatsubArgs.SyncArgs(
          None,
          sync,
          srcFile,
          out,
        ) =>
      val firstLine = FileF.firstLineFromFile(srcFile.value)
      firstLine match {
        case Some(line) =>
          val trimmed = line.trim.nn
          if trimmed.equalsIgnoreCase("<SAMI>") then
            WhatsubArgs.SyncArgs(
              SyncArgs.Sub(SupportedSub.Smi).some,
              sync,
              srcFile,
              out,
            )
          else if trimmed.equalsIgnoreCase("1") then
            WhatsubArgs.SyncArgs(
              SyncArgs.Sub(SupportedSub.Srt).some,
              sync,
              srcFile,
              out,
            )
          else arg

        case None =>
          arg
      }

    case whatsubArgs =>
      whatsubArgs
  }

  def charsetTaskSubcommand: Parse[WhatsubArgs] = (subcommand(
    Command(
      "list",
      "List all available charsets".some,
      charsetListParse,
    ),
  ) ||| subcommand(
    Command(
      "convert",
      "Convert charset to another".some,
      charsetConvertParse,
    ),
  )).map(WhatsubArgs.CharsetArgs(_))

  def charsetListParse: Parse[CharsetArgs.CharsetTask] = ValueParse(CharsetArgs.CharsetTask.ListAll)

  def charsetFromParse: Parse[CharsetArgs.From] = flag[CharsetArgs.From](
    both('f', "from"),
    metavar("<from>") |+| description(
      s"""The name of charset to be converted from (e.g. ${"Windows-949".blue} for ${"Korean".blue} charset)""",
    ),
  )

  def charsetToParse: Parse[CharsetArgs.To] = flag[CharsetArgs.To](
    both('t', "to"),
    metavar("<to>") |+| description(
      s"""The name of charset to be converted to (${"default".green}: ${"UTF-8".blue})"""
    ),
  ).default(CharsetArgs.To(Charset.Utf8.value))

  def charsetSrcFileParse: Parse[CharsetArgs.SrcFile] = argument[File](
    metavar("<src>") |+| description("The source subtitle file"),
  ).map(CharsetArgs.SrcFile(_))

  def charsetOutFileParse: Parse[Option[CharsetArgs.OutFile]] = argument[File](
    metavar("<out>") |+| description(
      s"""An ${"optional".green} output subtitle file. If missing, the result is printed out.""",
    ),
  ).option.map(_.map(CharsetArgs.OutFile(_)))

  def charsetConvertParse: Parse[CharsetArgs.CharsetTask] = CharsetArgs.CharsetTask.Convert.apply |*| (
    charsetFromParse,
    charsetToParse,
    charsetSrcFileParse,
    charsetOutFileParse,
  )

  val rawCmd: Command[WhatsubArgs] =
    Command(
      "Whatsub",
      "A tool to convert subtitles and re-sync".some,
      (
        subcommand(
          Command(
            "convert",
            "Convert subtitles".some,
            convertParse,
          ),
        ) ||| subcommand(
          Command(
            "sync",
            "sync subtitles".some,
            syncArgsParse,
          ),
        ) ||| subcommand(
          Command(
            "charset",
            "Charset conversion".some,
            charsetTaskSubcommand,
          ),
        ),
      ) <* version(WhatsubBuildInfo.version),
    )

  final case class JustMessageOrHelp(messages: List[String])
  object JustMessageOrHelp {
    given show: Show[JustMessageOrHelp] = _.messages.mkString("\n")
  }
  final case class ArgParseError(errors: List[String])
  object ArgParseError {
    given show: Show[ArgParseError] = _.errors.mkString("\n")
  }

  type ArgParseFailureResult =
    JustMessageOrHelp | ArgParseError

}
