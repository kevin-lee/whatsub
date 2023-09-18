package whatsub

import cats.Monad
import cats.data.EitherT
import cats.effect.*
import cats.effect.kernel.MonadCancel
import cats.syntax.all.*
import effectie.core.*
import effectie.syntax.all.{*, given}
import extras.cats.syntax.all.*
import extras.scala.io.syntax.color.*
import whatsub.FileF.FileError

import java.io.{BufferedWriter, File, FileWriter, Writer}
import scala.util.control.NonFatal

/** @author Kevin Lee
  * @since 2021-07-06
  */
trait FileF[F[*]] {
  def writeFile[A: CanRender](a: A, file: File, name: Option[String]): F[Either[FileError, Unit]]

  def writeFileWith[E](
    file: File,
    successMessage: => String,
    fileErrorHandler: FileError => E
  )(f: Writer => F[Either[E, Unit]]): F[Either[E, Unit]]
}

object FileF {

  def apply[F[*]: FileF]: FileF[F] = summon[FileF[F]]

  given fileF[F[*]: Monad: MCancel: Fx]: FileF[F] with {

    override def writeFile[A: CanRender](a: A, file: File, name: Option[String]): F[Either[FileError, Unit]] =
      writeFileWith[FileError](
        file,
        s""">> [${"Success".green}] ${name.getOrElse("The file")} has been successfully written at
           |>>   ${file.getCanonicalPath.nn.blue.bold}
           |""".stripMargin,
        identity,
      ) { writer =>
        (for {
          content <- effectOf(CanRender[A].render(a)).rightT
          _       <- effectOf(writer.write(content)).rightT
        } yield ()).value
      }

    override def writeFileWith[E](
      file: File,
      successMessage: => String,
      fileErrorHandler: FileError => E
    )(f: Writer => F[Either[E, Unit]]): F[Either[E, Unit]] =
      Resource
        .make(effectOf(new BufferedWriter(new FileWriter(file))))(writer => effectOf(writer.close()))
        .use { writer =>
          f(writer)
            .t
            .catchNonFatalEitherT {
              case NonFatal(th) =>
                fileErrorHandler(FileError.WriteFailure(file, th))
            }
            .flatMap { _ =>
              putStrLn(successMessage).rightT
            }
            .value
        }

  }

  @SuppressWarnings(Array("org.wartremover.warts.PlatformDefault"))
  def firstLineFromFile(file: File): Option[String] =
    util.Using(scala.io.Source.fromFile(file))(_.getLines.find(_ => true)).toOption.flatten

  enum FileError derives CanEqual {
    case WriteFailure(file: File, throwable: Throwable)
  }

  object FileError {

    extension (fileError: FileError) {
      def render: String = fileError match {
        case FileError.WriteFailure(file, throwable) =>
          s"""Error when writing file at ${file.getCanonicalPath}
             |Reason: ${throwable.getMessage}
             |""".stripMargin
      }
    }
  }

}
