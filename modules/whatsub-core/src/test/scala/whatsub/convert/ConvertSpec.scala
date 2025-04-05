package whatsub.convert

import cats.*
import cats.effect.IO
import effectie.instances.ce3.fx.given
import effectie.core.Fx
import extras.cats.syntax.all.*
import extras.hedgehog.ce3.CatsEffectRunner
import hedgehog.*
import hedgehog.runner.*
import whatsub.parse.{ParseError, SmiParser, SrtParser}
import whatsub.{CanRender, GoldenTest, Smi, Srt}

import scala.reflect.*

/** @author Kevin Lee
  * @since 2022-03-05
  */
object ConvertSpec extends Properties with CatsEffectRunner {
  override def tests: List[Prop] = List(
    goldenTestConvert[Smi, Srt]("golden/test-src.smi", "golden/test-out.srt", SmiParser.parse[IO]),
    goldenTestConvert[Srt, Smi]("golden/test-src.srt", "golden/test-out.smi", SrtParser.parse[IO]),
  )

  private def testGolden[F[*]: Fx: Monad, A, B: CanRender](
    srcFile: String,
    outFile: String,
    aParser: Seq[String] => F[Either[ParseError, A]],
  )(using Convert[F, A, B]): F[Result] =
    GoldenTest.goldenTestF[F](
      srcFile,
      outFile,
    ) { (input, expected) =>
      val converter = Convert[F, A, B]
      (for {
        a <- aParser(input.value)
               .t
               .leftMap(_.render)
        b <- converter
               .convert(a)
               .t
               .leftMap(_.render)
      } yield b)
        .fold(
          err => Result.failure.log(err),
          actual => actual.render.trim ==== expected.value,
        )
    }

  def goldenTestConvert[A: ClassTag, B: CanRender: ClassTag](
    srcFile: String,
    outFile: String,
    aParser: Seq[String] => IO[Either[ParseError, A]],
  )(using Convert[IO, A, B]): Test = {
    val aClass = classTag[A].runtimeClass
    val bClass = classTag[B].runtimeClass
    val name   = s"golden test - Convert[F, ${aClass.getSimpleName}, ${bClass.getSimpleName}]"

    val result = testGolden[IO, A, B](srcFile, outFile, aParser)

    example(
      name,
      withIO { implicit ticker =>
        result.completeThen(identity)
      },
    )
  }

}
