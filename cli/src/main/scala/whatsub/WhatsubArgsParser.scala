package whatsub

import scalaz.*
import Scalaz.*
import pirate.{ParseError, *}
import Pirate.*
import WhatsubArgs.*
import pirate.internal.ParseTraversal
import whatsub.info.WhatsubBuildInfo

import java.io.File

/** @author Kevin Lee
  * @since 2021-07-01
  */
object WhatsubArgsParser {

  def fromParse: Parse[ConvertArgs.From] = flag[ConvertArgs.From](
    both('f', "from"),
    metavar("<from>") |+| description("A type of subtitle to be converted from"),
  )

  def toParse: Parse[ConvertArgs.To] = flag[ConvertArgs.To](
    both('t', "to"),
    metavar("<to>") |+| description("A type of subtitle to be converted to"),
  )

  def srcFileParse: Parse[ConvertArgs.SrcFile] = flag[File](
    both('s', "src"),
    metavar("<src>") |+| description("The source subtitle file"),
  ).map(ConvertArgs.SrcFile(_))

  def outFileParse: Parse[Option[ConvertArgs.OutFile]] = flag[File](
    both('o', "out"),
    metavar("<out>") |+| description("An optional output subtitle file. If missing, the result is printed out."),
  ).option.map(_.map(ConvertArgs.OutFile(_)))

  def convertParse: Parse[WhatsubArgs] = WhatsubArgs.ConvertArgs.apply |*| (
    fromParse,
    toParse,
    srcFileParse,
    outFileParse,
  )

  def subParse: Parse[SyncArgs.Sub] = flag[SyncArgs.Sub](
    both('t', "sub-type"),
    metavar("<sub-type>") |+| description("A type of subtitle. Either smi or srt"),
  )

  def syncParse: Parse[SyncArgs.Sync] = flag[SyncArgs.Sync](
    both('m', "sync"),
    metavar("<sync>") |+| description(
      "resync playtime (e.g. shift 1 hour 12 minutes 3 seconds 100 milliseconds forward: +1h12m3s100ms",
    ),
  )

  def syncSrcFileParse: Parse[SyncArgs.SrcFile] = flag[File](
    both('s', "src"),
    metavar("<src>") |+| description("The source subtitle file"),
  ).map(SyncArgs.SrcFile(_))

  def syncOutFileParse: Parse[Option[SyncArgs.OutFile]] = flag[File](
    both('o', "out"),
    metavar("<out>") |+| description("An optional output subtitle file. If missing, the result is printed out."),
  ).option.map(_.map(SyncArgs.OutFile(_)))

  def syncArgsParse: Parse[WhatsubArgs] = SyncArgs.apply |*| (
    subParse,
    syncParse,
    syncSrcFileParse,
    syncOutFileParse,
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
        ),
      ) <* version(WhatsubBuildInfo.version),
    )
}
