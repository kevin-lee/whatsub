
import cats.effect.kernel.MonadCancel

import java.io.File

/** @author Kevin Lee
  * @since 2021-07-25
  */
package object whatsub {
  type MCancel[F[_]] = MonadCancel[F, Throwable]

  given fileCanEqual: CanEqual[File, File] = CanEqual.derived
}
