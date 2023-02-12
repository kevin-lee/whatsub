package whatsub.parse

import cats.effect.IO
import effectie.instances.ce3.fx.given
import hedgehog.*
import hedgehog.runner.*
import whatsub.parse.SubParsers.*
import whatsub.*

/** @author Kevin Lee
  * @since 2023-02-12
  */
object SrtParserSpec extends Properties {
  override def tests: List[Test] = List(
    ParserGoldenTester.goldenTestParse[Srt]("golden/test-src.srt", "golden/test-src.srt", SrtParser.parse[IO]),
    ParserGoldenTester.goldenTestParse[Srt](
      "golden/test-srt-parse-with-empty-lines.srt",
      "golden/test-srt-parse-with-empty-lines.srt",
      SrtParser.parse[IO]
    ),
  )

}
