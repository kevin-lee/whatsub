package whatsub.ai.translate

import cats.Monad

import effectie.core.Fx
import extras.testing.StubToolsFx

import cats.syntax.all.*

import openai4s.api.chat.ChatApi
import openai4s.types.chat.{Chat, Response}

/** @author Kevin Lee
  * @since 2023-09-16
  */
object ChatApiStub {
  def apply[F[*]: Fx: Monad](f: => Option[Chat => F[Response]]): ChatApi[F] = new ChatApi[F]:
    override def completion(chat: Chat): F[Response] =
      StubToolsFx.stub[F](f).flatMap(_(chat))
}
