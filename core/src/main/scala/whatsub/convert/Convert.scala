package whatsub.convert

import cats.Applicative
import cats.syntax.all.*
import effectie.cats.Effectful.*
import extras.cats.syntax.all.*
import effectie.cats.Fx
import whatsub.{Smi, Srt, SupportedSub}

/** @author Kevin Lee
  * @since 2021-06-18
  */
trait Convert[F[_], A, B] {
  def convert(a: A): F[Either[ConversionError, B]]
}

object Convert {

  def apply[F[_], A, B](using Convert[F, A, B]): Convert[F, A, B] = summon[Convert[F, A, B]]

  given smiToSrtConvert[F[_]: Fx: Applicative]: Convert[F, Smi, Srt] =
    smi =>
      (if (smi.lines.isEmpty)
         ConversionError
           .noContent(
             SupportedSub.Smi,
             s"""The smi titled "${smi.title}"""",
           )
           .leftTF[F, Srt]
       else
         effectOf(
           for {
             (smiLine, index) <- smi.lines.zipWithIndex
             start             = smiLine.start.value
             end               = smiLine.end.value
             line              = smiLine.line.value
           } yield Srt.SrtLine(
             Srt.Index(index + 1),
             Srt.Start(start),
             Srt.End(end),
             Srt.Line(line),
           ),
         ).rightT[ConversionError].map(Srt(_))).value

  given srtToSmiConvert[F[_]: Fx: Applicative]: Convert[F, Srt, Smi] =
    srt =>
      if (srt.lines.isEmpty) {
        pureOf(
          ConversionError
            .noContent(
              SupportedSub.Srt,
              s"""The srt"""",
            )
            .asLeft[Smi],
        )
      } else {
        val lines = srt.lines.map {
          case Srt.SrtLine(_, start, end, line) =>
            Smi.SmiLine(
              Smi.Start.fromSrt(start),
              Smi.End.fromSrt(end),
              Smi.Line.fromSrt(line),
            )
        }
        pureOf(
          Smi(
            Smi.Title(""),
            lines,
          ).asRight[ConversionError],
        )
      }
}
