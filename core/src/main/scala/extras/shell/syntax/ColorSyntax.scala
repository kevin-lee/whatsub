package extras.shell.syntax

import extras.shell.Color

/** Copied from Kevin-Lee/jdk-sym-link (https://git.io/JEKu0) and modified.
 * @author Kevin Lee
 * @since 2020-01-01
 */
trait ColorSyntax {

  extension (text: String) {

    def colored(color: Color): String =
      s"${color.toAnsi}$text${Color.Reset.toAnsi}"

    def bold: String       = colored(Color.Bold)
    def underlined: String = colored(Color.Underlined)

    def red: String   = colored(Color.Red)
    def green: String = colored(Color.Green)
    def blue: String  = colored(Color.Blue)
  }

}
