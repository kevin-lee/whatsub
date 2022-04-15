package whatsub

import effectie.core.*
import effectie.syntax.all.*
import hedgehog.*
import hedgehog.runner.*
import whatsub.parse.SubParsers.*

import java.io.File
import scala.io.{Codec, Source}
import scala.util.Using

/** @author Kevin Lee
  * @since 2022-03-05
  */
object GoldenTest {

  type Input = Input.Input
  object Input {
    opaque type Input = List[String]
    def apply(input: List[String]): Input = input

    given inputCanEqual: CanEqual[Input, Input] = CanEqual.derived

    extension (input: Input) {
      def value: List[String] = input
    }
  }

  type Expected = Expected.Expected
  object Expected {
    opaque type Expected = String
    def apply(expected: String): Expected = expected

    given expectedCanEqual: CanEqual[Expected, Expected] = CanEqual.derived

    extension (expected: Expected) {
      def value: String = expected
    }
  }

  def goldenTestF[F[*]: Fx](src: String, out: String)(f: (Input, Expected) => F[Result]): F[Result] =
    Using(Source.fromResource(src)(Codec.UTF8))(_.getLines.toList).toEither match {
      case Right(source) =>
        Using(Source.fromResource(out)(Codec.UTF8))(_.mkString).toEither match {
          case Right(expected) =>
            f(Input(source), Expected(expected.removeEmptyChars.replace("\r\n", "\n").trim))

          case Left(err) =>
            pureOf(
              Result.failure.log(s"Failed to load the golden file ($out). Error: ${err.getMessage}"),
            )
        }
      case Left(err) =>
        pureOf(
          Result
            .failure
            .log(s"Failed to load the src file ($src) for testing. Error: ${err.getMessage}"),
        )
    }

}
