package whatsub

import cats.effect.*
import cats.syntax.all.*
import pirate.{ExitCode as PirateExitCode, Command, Prefs, Runners}
import piratex.{Help, Metavar}

trait MainIo[A] extends IOApp {

  def command: Command[A]

  def runApp(a: A): IO[Either[WhatsubError, Unit]]

  def prefs: Prefs

  def exitCodeToEither(exitCode: PirateExitCode): IO[Either[WhatsubError, Unit]] =
    exitCode.fold(
      IO.pure(().asRight[WhatsubError]),
      code => IO(WhatsubError.FailedWithExitCode(code).asLeft[Unit]),
    )

  override def run(args: List[String]): IO[ExitCode] = {
    def getArgs(
      args: List[String],
      command: Command[A],
      prefs: Prefs,
    ): IO[Either[PirateExitCode, A]] =
      IO(Runners.runWithExit[A](args, command, prefs).unsafePerformIO().toEither)

    for {
      codeOrA       <- getArgs(args, command, prefs)
      errorOrResult <- codeOrA.fold(exitCodeToEither, runApp)
      code          <- errorOrResult.fold(
                         err =>
                           IO(System.err.println(err.render)) >>
                             IO(ExitCode.Error),
                         _ => IO(ExitCode.Success),
                       )
    } yield code
  }

}
