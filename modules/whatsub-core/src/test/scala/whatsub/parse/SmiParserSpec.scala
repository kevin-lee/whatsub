package whatsub.parse

import cats.effect.IO
import effectie.instances.ce3.fx.given
import hedgehog.*
import hedgehog.runner.*
import whatsub.*

/** @author Kevin Lee
  * @since 2023-02-12
  */
object SmiParserSpec extends Properties {
  override def tests: List[Test] = List(
    ParserGoldenTester.goldenTestParse[Smi]("golden/test-src.smi", "golden/test-smi-parsed.smi", SmiParser.parse[IO]),
  )

}
