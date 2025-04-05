package whatsub.ai.translate

import cats.Monad
import cats.data.NonEmptyList
import cats.syntax.all.*
import effectie.core.Fx
import effectie.syntax.all.*
import extras.render.Render
import extras.render.syntax.*
import loggerf.core.Log
import loggerf.syntax.all.*
import openai4s.api.chat.ChatApi
import openai4s.types.chat.Message
import openai4s.types.common.{Temperature, MaxTokens}
import openai4s.types.chat.{Chat, Model}
import refined4s.*
import refined4s.types.all.*
import refined4s.modules.extras.derivation.*
import refined4s.modules.extras.derivation.types.all.given
import refined4s.modules.cats.derivation.*
import refined4s.modules.cats.derivation.types.all.given
import whatsub.ai.translate.Translator.Language
import whatsub.core.SubLine
import whatsub.{Smi, Srt, core}

import scala.util.matching.Regex

/** @author Kevin Lee
  * @since 2023-07-14
  */
trait Translator[F[*]] {
  def translate[A](sub: A, language: Language)(using A <:< (Smi | Srt)): F[A]
}
object Translator {

  val LinePattern: Regex = """^@T@-([\d]+)[\s]+(.*)""".r

  def apply[F[*]: Monad: Fx: Log](chatApi: ChatApi[F]): Translator[F] = new TranslatorF[F](chatApi)

  private final class TranslatorF[F[*]: Monad: Fx: Log](chatApi: ChatApi[F]) extends Translator[F] {

    @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
    private def translateWithAi(chat: Chat, acc: Vector[Message.Content]): F[Vector[Message.Content]] =
      for {
        _      <- s">>> Chat: ${chat.messages.mkString_("\n")}".logS_(info)
        res    <- chatApi
                    .completion(chat)
                    .log(res =>
                      info(
                        show""">>> Response:
                           |$res
                           |---
                           |""".stripMargin
                      )
                    )
        result <- res
                    .choices
                    .lastOption
                    .fold(
                      pureOf(res.choices.map(_.message.content).toVector)
                    ) { choice =>
                      if (choice.finishReason.value === "length") {
                        val lines = choice.message.content.value.split("\n")
                        val index = lines.lastIndexWhere(_.startsWith("@T@-"))
                        if (index < 0) pureOf(acc)
                        else {
//                          val last = lines.drop(index).mkString("\n")
                          render">>> index: ${choice.index}".logS_(info) *>
                            render">>> finishReason: ${choice.finishReason}".logS_(info) *>
                            show">>> message: ${choice.message}".logS_(info) *>
                            translateWithAi(
                              chat.copy(messages =
                                chat.messages.append(choice.message) |+|
                                  NonEmptyList.of(
                                    Message(
                                      Message.Role("user"),
                                      Message.Content(
                                        "Continue"
//                                      s"Continue from the last subtitle translation. The last line I got was the following.\n$last"
                                      )
                                    )
                                  )
                              ),
                              acc :+ choice.message.content,
                            )

                        }
                      } else pureOf(acc :+ choice.message.content)
                    }
      } yield result

    @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
    override def translate[A](sub: A, language: Language)(using A <:< (Smi | Srt)): F[A] = sub match {
      case srt @ Srt(lines) =>
        val srtLines = lines
        val subLines = srtLines.map(_.toSubLine).toVector
        val content  = buildMessageContent(language, subLines)
        val chat     = Chat(
          model = Model.gpt_4_1106_Preview,
          messages = NonEmptyList.of(
            Message(
              Message.Role("user"),
              content,
            )
          ),
          temperature = Temperature(0.0f).some,
          maxTokens = MaxTokens(128_000).some,
        )
        for {
          contents <- translateWithAi(chat, Vector.empty)
          response = contents.mkString
          lines      <- pureOrError(response.split("\n"))
          translated <- collectSubs(subLines, lines.toList)
        } yield Srt(translated.map(Srt.SrtLine.fromSubLine).toList)
          .asInstanceOf[A] // scalafix:ok DisableSyntax.asInstanceOf

      case smi @ Smi(title, lines) =>
        val smiLines = lines
        val subLines = smiLines.zipWithIndex.map { case (smiLine, index) => smiLine.toSubLine(index) }.toVector
        val content  = buildMessageContent(language, subLines)
        val chat     = Chat(
          model = Model.gpt_4_1106_Preview,
          messages = NonEmptyList.of(
            Message(
              Message.Role("User"),
              content,
            )
          ),
          temperature = Temperature(0.0f).some,
          maxTokens = MaxTokens(128_000).some,
        )
        for {
          contents <- translateWithAi(chat, Vector.empty)
          response = contents.mkString
          lines      <- pureOrError(response.split("\n"))
          translated <- collectSubs(subLines, lines.toList)
        } yield Smi(title, translated.map(Smi.SmiLine.fromSubLine).toList)
          .asInstanceOf[A] // scalafix:ok DisableSyntax.asInstanceOf
    }

  }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  def collectSubs[F[*]: Fx: Monad](subLines: Vector[SubLine], lines: List[String]): F[Vector[SubLine]] = {
    def processWithLast(
      lines: List[String],
      lastIndex: Int,
      lastLine: SubLine,
      acc: Vector[SubLine]
    ): F[Vector[SubLine]] =
      lines match {
        case LinePattern(index, line) :: rest =>
          val n = index.toInt

          @SuppressWarnings(Array("org.wartremover.warts.SeqApply"))
          val subLine = subLines(n)
          val last    = SubLine(subLine.index, subLine.start, subLine.end, SubLine.Line(line))
          processWithLast(rest, n, last, acc :+ lastLine)

        case anythingElse :: rest =>
          processWithLast(
            rest,
            lastIndex,
            lastLine.copy(line = SubLine.Line(lastLine.line.value + "\n" + anythingElse)),
            acc
          )

        case Nil =>
          pureOf(acc :+ lastLine)
      }

    def process(lines: List[String], acc: Vector[SubLine]): F[Vector[SubLine]] = lines match {
      case LinePattern(index, line) :: rest =>
        val n = index.toInt

        @SuppressWarnings(Array("org.wartremover.warts.SeqApply"))
        val subLine = subLines(n)
        processWithLast(rest, n, SubLine(subLine.index, subLine.start, subLine.end, SubLine.Line(line)), acc)

      case anythingElse :: rest =>
        println(s"skip> $anythingElse")
        process(rest, acc)

      case Nil =>
        pureOf(acc)
    }
    process(lines, Vector.empty)
  }

  private[translate] def buildMessageContent[A](language: Language, subLines: Vector[SubLine]): Message.Content =
    Message.Content(
      render"Translate into ${language.toValue}. " +
        "Each line starts with @S@-index (e.g. @S@-0, @S@-1, @S@-2, etc.) so the translated one should starts with @T@-index (e.g. @T@-0, @T@-1, @T@-2, etc.) " +
        "Your answer should have only the translated text without saying anything else.\n"
        + subLines
          .zipWithIndex
          .map {
            case (line, index) =>
              s"@S@-${index.render} ${line.line.value}"
          }
          .mkString("\n")
    )

  type Language = Language.Type
  object Language extends Newtype[NonEmptyString], ExtrasRender[NonEmptyString], CatsEqShow[NonEmptyString]

}
