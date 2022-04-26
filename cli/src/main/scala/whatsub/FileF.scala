package whatsub

import cats.Monad
import cats.data.EitherT
import cats.effect.*
import cats.effect.kernel.MonadCancel
import cats.syntax.all.*
import effectie.core.*
import effectie.syntax.all.*
import effectie.cats.console.given
import extras.cats.syntax.all.*
import whatsub.FileF.FileError

import java.io.{BufferedWriter, File, FileWriter}
import scala.util.control.NonFatal

/** @author Kevin Lee
  * @since 2021-07-06
  */
trait FileF[F[*]] {
  def writeFile[A: CanRender](a: A, file: File): F[Either[FileError, Unit]]
}

object FileF {

  def apply[F[*]: FileF]: FileF[F] = summon[FileF[F]]

  given fileF[F[*]: Monad: MCancel: Fx]: FileF[F] with {

    def writeFile[A: CanRender](a: A, file: File): F[Either[FileError, Unit]] =
      Resource
        .make(effectOf(new BufferedWriter(new FileWriter(file))))(writer => effectOf(writer.close()))
        .use { writer =>
          (for {
            content <- effectOf(CanRender[A].render(a)).rightT
            _       <- CanCatch[F]
                         .catchNonFatal(effectOf(writer.write(content))) {
                           case NonFatal(th) =>
                             FileError.WriteFailure(file, th)
                         }
                         .eitherT
            _       <- putStrLn(
                         s"""Success] The subtitle file has been successfully written at
                            |  ${file.getCanonicalPath}
                            |""".stripMargin,
                       ).rightT[FileError]
          } yield ()).value
        }
  }

  @SuppressWarnings(Array("org.wartremover.warts.PlatformDefault"))
  def firstLineFromFile(file: File): Option[String] =
    util.Using(io.Source.fromFile(file))(_.getLines.find(_ => true)).toOption.flatten

  enum FileError derives CanEqual {
    case WriteFailure(file: File, throwable: Throwable)
  }

  object FileError {

    extension (fileError: FileError) {
      def render: String = fileError match {
        case FileError.WriteFailure(file, throwable) =>
          s"Error when writing file at ${file.getCanonicalPath}. Error: ${throwable.getMessage}"
      }
    }
  }

}
