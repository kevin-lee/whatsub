logLevel := sbt.Level.Warn

addSbtPlugin("org.scalameta" % "sbt-native-image" % "0.3.0")
addSbtPlugin("com.eed3si9n"  % "sbt-buildinfo"    % "0.10.0")

val sbtDevOopsVersion = "2.6.0"
addSbtPlugin("io.kevinlee" % "sbt-devoops-scala"     % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-sbt-extra" % sbtDevOopsVersion)
addSbtPlugin("io.kevinlee" % "sbt-devoops-github"    % sbtDevOopsVersion)
