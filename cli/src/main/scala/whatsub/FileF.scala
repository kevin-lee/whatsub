package whatsub

import cats.data.EitherT
import cats.effect.*
import cats.syntax.all.*
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

  def fileF[F[_]: Sync]: FileF[F] = new FileFSync[F]

  final class FileFSync[F[_]: Sync] extends FileF[F] {

    def writeFile[A: CanRender](a: A, file: File): F[Either[FileError, Unit]] =
      Resource
        .make(Sync[F].delay(new BufferedWriter(new FileWriter(file))))(writer => Sync[F].delay(writer.close()))
        .use { writer =>
          (for {
            content <- EitherT.right(Sync[F].delay(CanRender[A].render(a)))
            _       <- EitherT(
                         Sync[F]
                           .delay(writer.write(content))
                           .attempt,
                       ).leftMap {
                         case NonFatal(th) =>
                           FileError.WriteFilure(file, th)
                       }
            _       <- EitherT.right[FileError](
                         Sync[F].delay(
                           println(
                             s"""Success] The subtitle file has been successfully written at
                                |  ${file.getCanonicalPath}
                                |""".stripMargin,
                           ),
                         ),
                       )
          } yield ()).value
        }
  }
}
