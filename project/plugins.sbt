logLevel := sbt.Level.Warn

addSbtPlugin("com.github.sbt"  % "sbt-native-packager" % "1.9.16")
addSbtPlugin("org.scalameta"   % "sbt-native-image"    % "0.3.0")
addSbtPlugin("com.eed3si9n"    % "sbt-buildinfo"       % "0.11.0")
addSbtPlugin("org.wartremover" % "sbt-wartremover"     % "3.3.2")
addSbtPlugin("org.scalameta"   % "sbt-scalafmt"        % "2.5.0")
addSbtPlugin("ch.epfl.scala"   % "sbt-scalafix"        % "0.11.0")

addSbtPlugin("io.kevinlee" % "sbt-docusaur" % "0.17.0")

val sbtDevOopsVersion = "3.2.1"
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-github"    % sbtDevOopsVersion)

addSbtPlugin("io.kevinlee" % "sbt-devoops-starter" % sbtDevOopsVersion)

addSbtPlugin("org.typelevel" % "sbt-tpolecat" % "0.5.2")
