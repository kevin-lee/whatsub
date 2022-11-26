package whatsub

import cats.Show
import cats.effect.*
import cats.syntax.all.*
import effectie.syntax.all.{*, given}
import effectie.instances.ce3.fx.given
import effectie.instances.console.*
import pirate.{Command, Prefs, Runners, ExitCode as PirateExitCode}
import piratex.{Help, Metavar}
import whatsub.WhatsubArgsParser.{ArgParseError, ArgParseFailureResult, JustMessageOrHelp}

trait MainIo[A] extends IOApp {

  def command: Command[A]

  def runApp(a: A): IO[Either[WhatsubError, Option[String]]]

  def prefs: Prefs

  def exitCodeToEither(argParseFailureResult: ArgParseFailureResult): IO[Either[WhatsubError, Option[String]]] =
    argParseFailureResult match {
      case err @ JustMessageOrHelp(_) =>
        IO.pure(err.show.some.asRight[WhatsubError])
      case err @ ArgParseError(_) =>
        IO(WhatsubError.ArgParse(err).asLeft[Option[String]])
    }

  override def run(args: List[String]): IO[ExitCode] = {
    def getArgs(
      args: List[String],
      command: Command[A],
      prefs: Prefs,
    ): IO[Either[ArgParseFailureResult, A]] = {
      import scalaz.{-\/, \/-}
      import pirate.Interpreter
      import pirate.Usage
      Interpreter.run(command.parse, args, prefs) match {
        case (ctx, -\/(e)) =>
          IO(
            Usage
              .printError(command, ctx, e, prefs)
              .fold[ArgParseFailureResult](
                ArgParseError(_),
                JustMessageOrHelp(_),
              )
              .asLeft[A],
          )
        case (_, \/-(v)) =>
          IO(v.asRight[ArgParseFailureResult])
      }
    }
    for {
      codeOrA       <- getArgs(args, command, prefs)
      errorOrResult <- codeOrA.fold(exitCodeToEither, runApp)
      code          <- errorOrResult.fold(
                         err =>
                           putErrStrLn[IO](err.render) >>
                             IO(ExitCode.Error),
                         _.fold(
                           IO(ExitCode.Success),
                         )(msg => putStrLn[IO](msg) >> IO(ExitCode.Success)),
                       )
    } yield code
  }

}
