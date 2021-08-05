
import cats.effect.kernel.MonadCancel

/** @author Kevin Lee
  * @since 2021-07-25
  */
package object whatsub {
  type MCancel[F[_]] = MonadCancel[F, Throwable]
}
