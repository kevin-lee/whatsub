package whatsub.parse

import cats.Monad
import cats.effect.IO
import cats.syntax.all.*
import effectie.core.*
import effectie.instances.ce3.fx.given
import effectie.resource.{Ce3ResourceMaker, ResourceMaker}
import effectie.syntax.all.*
import extras.cats.syntax.all.*
import extras.hedgehog.ce3.CatsEffectRunner
import hedgehog.*
import hedgehog.runner.*
import whatsub.parse.SubParsers.*
import whatsub.{CanRender, FileF, GoldenTest, Smi, Srt}

import java.io.File
import scala.io.{Codec, Source}
import scala.reflect.{ClassTag, classTag}

/** @author Kevin Lee
  * @since 2023-02-12
  */
object SrtParserSpec extends Properties {
  override def tests: List[Test] = List(
    ParserGoldenTester.goldenTestParse[Srt]("golden/test-src.srt", "golden/test-src.srt", SrtParser.parse[IO]),
    ParserGoldenTester.goldenTestParse[Srt](
      "golden/test-src-with-empty-lines.srt",
      "golden/test-src-with-empty-lines.srt",
      SrtParser.parse[IO]
    ),
  )

}
