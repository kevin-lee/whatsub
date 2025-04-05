package whatsub.parse

import cats.Monad
import cats.effect.IO
import effectie.core.*
import effectie.instances.ce3.fx.given
import extras.cats.syntax.all.*
import extras.hedgehog.ce3.CatsEffectRunner
import hedgehog.*
import hedgehog.runner.*
import whatsub.*

import scala.reflect.{ClassTag, classTag}

/** @author Kevin Lee
  * @since 2023-02-12
  */
object ParserGoldenTester extends CatsEffectRunner {

  private def testGolden[F[*]: Fx: Monad, A: CanRender](
    srcFile: String,
    outFile: String,
    aParser: Seq[String] => F[Either[ParseError, A]],
  ): F[Result] =
    GoldenTest.goldenTestF[F](
      srcFile,
      outFile,
    ) { (input, expected) =>
      (for {
        a <- aParser(input.value)
               .t
               .leftMap(_.render)
      } yield a)
        .fold(
          err => Result.failure.log(err),
          actual => actual.render.replace("\r\n", "\n").trim ==== expected.value,
        )
    }

  def goldenTestParse[A: CanRender: ClassTag](
    srcFile: String,
    outFile: String,
    aParser: Seq[String] => IO[Either[ParseError, A]],
  ): Test = {
    val aClass = classTag[A].runtimeClass
    val name   = s"golden test - Parse ${aClass.getSimpleName}"

    val result = testGolden[IO, A](srcFile, outFile, aParser)

    example(
      name,
      withIO { implicit ticker =>
        result.completeThen(identity)
      },
    )
  }
}
