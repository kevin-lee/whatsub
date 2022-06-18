package whatsub

import cats.Show
import cats.data.NonEmptyList
import cats.syntax.all.*
import extras.scala.io.syntax.color.*
import whatsub.FileF
import whatsub.WhatsubArgsParser.ArgParseError
import whatsub.charset.{CharsetConvertError, ConvertCharset}
import whatsub.convert.ConversionError
import whatsub.parse.ParseError

import java.io.File

/** @author Kevin Lee
  * @since 2021-06-30
  */
enum WhatsubError {
  case ConversionFailure(conversionError: ConversionError)

  case ParseFailure(parseError: ParseError)

  case NoConversion(supportedSub: SupportedSub)

  case MissingSubTypes(typeToFile: NonEmptyList[(String, Option[File])])

  case FileWriteFailure(file: File, error: Throwable)

  case ArgParse(argError: ArgParseError)

  case CharsetConversion(charsetConvertError: CharsetConvertError)

  case IdenticalSrcAndOut(src: File, out: Option[File])

  case FileF(err: whatsub.FileF.FileError)
}

object WhatsubError {

  given whatsubErrorShow: Show[WhatsubError] = Show.fromToString

  extension (whatsubError: WhatsubError) {
    def render: String = whatsubError match {
      case WhatsubError.ConversionFailure(conversionError) =>
        s""">> ${"Conversion Failed".red}:
           |>> ${conversionError.render}
           |""".stripMargin

      case WhatsubError.ParseFailure(parseError) =>
        s""">> ${"Parsing sub failed".red}:
           |>> ${parseError.render}
           |""".stripMargin

      case WhatsubError.NoConversion(supportedSub) =>
        s""">> ${"No conversion".red}: The subtitle to convert from and to are the same (i.e. ${supportedSub.show})
           |""".stripMargin

      case WhatsubError.MissingSubTypes(typeToFile) =>
        typeToFile
          .map {
            case (what, Some(file)) =>
              s">> ${s"Missing $what type".red}: There is no $what type set nor is the $what type info found from the file (${file.toString}).\n"
            case (what, None) =>
              s">> ${s"Missing $what type".red}: There is no $what type set nor is there a file path given for the $what type.\n"
          }
          .toList
          .mkString("\n")

      case WhatsubError.FileWriteFailure(file: File, error: Throwable) =>
        s"""${"Error".red}: Writing file at ${file.getCanonicalPath} has failed with ${error.getMessage}"""

      case WhatsubError.ArgParse(error) =>
        s""">> ${"CLI arguments error".red}:
           |>> ${error.show}
           |""".stripMargin

      case WhatsubError.CharsetConversion(
            CharsetConvertError.Conversion(from, to, input, error),
          ) =>
        s""">> ${"Error when converting charset".red}
           |>>  From: $from
           |>>    To: $to
           |>> input: $input
           |>> error: ${error.getMessage}
           |""".stripMargin

      case WhatsubError.CharsetConversion(
            CharsetConvertError.Consumption(converted, error),
          ) =>
        s""">> ${"Error when consuming converted subtitle content".red}
           |>> converted: $converted
           |>>     error: ${error.getMessage}
           |""".stripMargin

      case WhatsubError.IdenticalSrcAndOut(src, out) =>
        s""">> ${"Invalid out filename or path".red}
           |>> The src file path and the out file path are the same. The out one should be different.
           |>> src: ${src.getCanonicalPath}
           |>> out: ${out.fold("")(_.getCanonicalPath)}
           |""".stripMargin

      case WhatsubError.FileF(err) =>
        s""">> [${"Error".red}] ${err.render.split("\n").mkString("\n>> ")}
           |""".stripMargin
    }
  }
}
