import cats.effect.MonadCancelThrow

import java.io.File

/** @author Kevin Lee
  * @since 2021-07-25
  */
package object whatsub {
  type MCancel[F[*]] = MonadCancelThrow[F]

  given fileCanEqual: CanEqual[File, File] = CanEqual.derived
}
