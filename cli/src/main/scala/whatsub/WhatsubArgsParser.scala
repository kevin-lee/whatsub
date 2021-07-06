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

  val rawCmd: Command[WhatsubArgs] =
    Command(
      "Whatsub",
      "A tool to convert subtitles and re-sync".some,
      (subcommand(
        Command(
          "convert",
          "Convert subtitles".some,
          convertParse,
        ),
      )) <* version(WhatsubBuildInfo.version),
    )
}
