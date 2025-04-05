package whatsub.ai.translate

import cats.effect.IO
import cats.syntax.all.*
import hedgehog.*
import hedgehog.runner.*
import effectie.syntax.all.*
import openai4s.types
import openai4s.types.common.*
import openai4s.types.chat.*
import refined4s.types.all.*
import whatsub.Srt
import extras.hedgehog.ce3.syntax.runner.*
import extras.render.syntax.*
import loggerf.logger.{CanLog, Slf4JLogger}
import whatsub.core.SubLine

import java.time.Instant

/** @author Kevin Lee
  * @since 2023-09-16
  */
object TranslatorSpec extends Properties {

  implicit val canLog: CanLog = Slf4JLogger.slf4JCanLog[TranslatorSpec.type]

  type F[A] = IO[A]
  val F = IO

  override def tests: List[Test] = List(
    property("test translate(Srt)", testTranslateSrt)
  )
  def testTranslateSrt: Property =
    for {
      language            <- Gen.string(Gen.alpha, Range.linear(1, 10)).log("language")
      subLinesAndExpected <- Gens.genSubLinesAndExpected.log("subLines, expected")
      (subLines, expectedSubLines) = subLinesAndExpected
    } yield runIO {
      import effectie.instances.ce3.fx.given
      import loggerf.instances.cats.given

      val requestMessageContent  =
        Translator.buildMessageContent(Translator.Language(NonEmptyString.unsafeFrom(language)), subLines.toVector)
      val responseMessageContent = Message
        .Content(
          expectedSubLines
            .map {
              case SubLine(index, _, _, line) =>
                s"@T@-${index.render} ${line.value}"
            }
            .mkString("\n")
        )

      val chatApi = ChatApiStub[F](
        (
          (_: Chat) => {
            effectOf(
              Response(
                Response.Id(NonEmptyString("test-id")),
                Response.Object(NonEmptyString("blah")),
                Response.Created(Instant.now()),
                Model.gpt_4,
                Response.Usage(
                  Response.Usage.PromptTokens(requestMessageContent.value.length),
                  Response.Usage.CompletionTokens(responseMessageContent.value.length),
                  Response.Usage.TotalTokens(requestMessageContent.value.length + responseMessageContent.value.length),
                ),
                List(
                  Response.Choice(
                    message = Message(
                      Message.Role("assistant"),
                      responseMessageContent
                    ),
                    finishReason = FinishReason("stop"),
                    index = Index(0)
                  )
                ),
              )
            )
          }
        ).some
      )

      val translator = Translator[F](chatApi)

      val expected = Srt(expectedSubLines.map(Srt.SrtLine.fromSubLine))
      translator
        .translate(Srt(subLines.map(Srt.SrtLine.fromSubLine)), Translator.Language(NonEmptyString("blah")))
        .map { actual =>
          actual ==== expected
        }
    }

}
