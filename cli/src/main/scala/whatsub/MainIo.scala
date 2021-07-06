package whatsub

import cats.effect.*
import cats.syntax.all.*

import pirate.{ExitCode => PirateExitCode, *}

trait MainIo[A] {

  def command: Command[A]

  def run(a: A): IO[Either[WhatsubError, Unit]]

  def prefs: Prefs = DefaultPrefs()

  def exitWith[X](exitCode: ExitCode): IO[X] =
    IO(sys.exit(exitCode.code))

  def exitWithPirate[X](exitCode: PirateExitCode): IO[X] =
    IO(exitCode.fold(sys.exit(0), sys.exit(_)))

  import cats.effect.unsafe.implicits.global
  def main(args: Array[String]): Unit = {
    import scalaz.*
    import scalaz.Scalaz.*

    def getArgs(args: Array[String], command: Command[A], prefs: Prefs): IO[PirateExitCode \/ A] =
      IO(Runners.runWithExit[A](args.toList, command, prefs).unsafePerformIO())

    def run0(a: A): IO[WhatsubError \/ Unit] = for {
      result    <- run(a)
      theResult <- IO(\/.fromEither[WhatsubError, Unit](result))
    } yield theResult

    (for {
      codeOrA       <- getArgs(args, command, prefs)
      errorOrResult <- codeOrA.fold[IO[WhatsubError \/ Unit]](exitWithPirate, run0)
      _             <- errorOrResult.fold(
                         err =>
                           IO(System.err.println(err.render)) >>
                             exitWith(ExitCode.Error),
                         IO(_),
                       )
    } yield ())
      .unsafeRunSync()
  }

}
