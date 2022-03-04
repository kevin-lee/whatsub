package whatsub

import cats.effect.IO
import cats.syntax.all.*
import pirate.{Command, DefaultPrefs, Prefs}
import piratex.{Help, Metavar}
import effectie.cats.fx.given

/** @author Kevin Lee
  * @since 2021-06-30
  */
object WhatsubApp extends MainIo[WhatsubArgs] {

  val cmd: Command[WhatsubArgs] =
    Metavar.rewriteCommand(
      Help.rewriteCommand(WhatsubArgsParser.rawCmd),
    )

  override def command: Command[WhatsubArgs] = cmd

  override def prefs: Prefs = DefaultPrefs().copy(width = 100)

  override def runApp(args: WhatsubArgs): IO[Either[WhatsubError, Option[String]]] =
    Whatsub[IO](args)
      .map(_.map(_ => none[String]))
}
