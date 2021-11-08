package whatsub

import cats.Monad
import cats.data.EitherT
import cats.effect.*
import cats.effect.kernel.MonadCancel
import cats.syntax.all.*
import effectie.cats.*
import effectie.cats.Effectful.*
import effectie.cats.ConsoleEffectful.*
import effectie.cats.EitherTSupport.*
import whatsub.FileF.FileError

import java.io.{BufferedWriter, File, FileWriter}
import scala.util.control.NonFatal

/** @author Kevin Lee
  * @since 2021-07-06
  */
trait FileF[F[_]] {
  def writeFile[A: CanRender](a: A, file: File): F[Either[FileError, Unit]]
}

object FileF {
  enum FileError derives CanEqual {
    case WriteFilure(file: File, throwable: Throwable)
  }

  object FileError {

    extension (fileError: FileError) {
      def render: String = fileError match {
        case FileError.WriteFilure(file, throwable) =>
          s"Error when writing file at ${file.getCanonicalPath}. Error: ${throwable.getMessage}"
      }
    }
  }

  def fileF[F[_]: Monad: MCancel: Fx: CanCatch]: FileF[F] = new FileFSync[F]

  final class FileFSync[F[_]: Monad: MCancel: Fx: CanCatch] extends FileF[F] {

    def writeFile[A: CanRender](a: A, file: File): F[Either[FileError, Unit]] =
      Resource
        .make(effectOf(new BufferedWriter(new FileWriter(file))))(writer => effectOf(writer.close()))
        .use { writer =>
          (for {
            content <- effectOf(CanRender[A].render(a)).rightT
            _       <- CanCatch[F]
                         .catchNonFatal(effectOf(writer.write(content))) {
                           case NonFatal(th) =>
                             FileError.WriteFilure(file, th)
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

  def firstLineFromFile(file: File): Option[String] =
    util.Using(io.Source.fromFile(file))(_.getLines.find(_ => true)).toOption.flatten
}
