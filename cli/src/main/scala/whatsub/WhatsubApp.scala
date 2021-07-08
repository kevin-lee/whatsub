package whatsub

import cats.effect.IO
import pirate.{Command, DefaultPrefs, Prefs}
import piratex.{Help, Metavar}

/** @author Kevin Lee
  * @since 2021-06-30
  */
object WhatsubApp extends MainIo[WhatsubArgs] {

  val cmd: Command[WhatsubArgs] =
    Metavar.rewriteCommand(
      Help.rewriteCommand(WhatsubArgsParser.rawCmd),
    )

  override def command: Command[WhatsubArgs] = cmd

  override def prefs: Prefs = DefaultPrefs()

  override def runApp(args: WhatsubArgs): IO[Either[WhatsubError, Unit]] =
    Whatsub[IO](args)
}
