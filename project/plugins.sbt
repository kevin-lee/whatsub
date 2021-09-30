logLevel := sbt.Level.Warn

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.9.4")
addSbtPlugin("org.scalameta"  % "sbt-native-image"    % "0.3.0")
addSbtPlugin("com.eed3si9n"   % "sbt-buildinfo"       % "0.10.0")

addSbtPlugin("io.kevinlee"   % "sbt-docusaur" % "0.8.0")

val sbtDevOopsVersion = "2.11.0"
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-github"    % sbtDevOopsVersion)
