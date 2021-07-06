ThisBuild / scalaVersion := props.ScalaVersion
ThisBuild / version := props.ProjectVersion
ThisBuild / organization := props.Org
ThisBuild / developers := List(
  Developer(
    props.GitHubUsername,
    "Kevin Lee",
    "kevin.code@kevinlee.io",
    url(s"https://github.com/${props.GitHubUsername}"),
  ),
)
ThisBuild / homepage := url(s"https://github.com/${props.GitHubUsername}/${props.RepoName}").some
ThisBuild / scmInfo :=
  ScmInfo(
    url(s"https://github.com/${props.GitHubUsername}/${props.RepoName}"),
    s"https://github.com/${props.GitHubUsername}/${props.RepoName}.git",
  ).some
ThisBuild / licenses := List("MIT" -> url("http://opensource.org/licenses/MIT"))

lazy val whatsub = (project in file("."))
  .settings(
    name := props.ProjectName,
  )
  .settings(noPublish)
  .aggregate(core, cli)

lazy val core = subProject("core", file("core"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    libraryDependencies ++=
      libs.catsAndCatsEffect3 ++ List(libs.catsParse),
    /* Build Info { */
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoObject := "WhatsubBuildInfo",
    buildInfoPackage := "whatsub.info",
    buildInfoOptions += BuildInfoOption.ToJson,
    /* } Build Info */
  )

lazy val pirate = ProjectRef(props.pirateUri, "pirate")

lazy val cli = subProject("cli", file("cli"))
  .enablePlugins(NativeImagePlugin)
  .settings(
    nativeImageOptions ++= Seq(
      "--verbose",
      "--no-fallback",
      "-H:+ReportExceptionStackTraces",
      "--initialize-at-build-time",
      //      s"-H:ReflectionConfigurationFiles=${ (sourceDirectory.value / "graal" / "reflect-config.json").getCanonicalPath }",
      //      "--allow-incomplete-classpath",
      //      "--report-unsupported-elements-at-runtime",
    ),
  )
  .dependsOn(core, pirate)

lazy val props =
  new {
    final val ScalaVersion = "3.0.0"
    final val Org          = "io.kevinlee"

    final val GitHubUsername = "Kevin-Lee"
    final val RepoName       = "whatsub"
    final val ProjectName    = RepoName
    final val ProjectVersion = "0.1.0-SNAPSHOT"

    final val HedgehogVersion = "0.7.0"

    final val CatsVersion        = "2.6.1"
    final val CatsEffect3Version = "3.1.1"

    final val CatsParseVersion = "0.3.4"

    final val pirateVersion = "main"
    final val pirateUri     = uri(s"https://github.com/$GitHubUsername/pirate.git#$pirateVersion")

    final val IncludeTest: String = "compile->compile;test->test"
  }

lazy val libs =
  new {
    lazy val hedgehogLibs = List(
      "qa.hedgehog" %% "hedgehog-core"   % props.HedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-runner" % props.HedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-sbt"    % props.HedgehogVersion % Test,
    )

    lazy val catsAndCatsEffect3 = List(
      "org.typelevel" %% "cats-core"   % props.CatsVersion,
      "org.typelevel" %% "cats-effect" % props.CatsEffect3Version,
    )

    lazy val catsParse = "org.typelevel" %% "cats-parse" % props.CatsParseVersion

  }

// format: off
def prefixedProjectName(name: String) = s"${props.RepoName}${if (name.isEmpty) "" else s"-$name"}"
// format: on

def subProject(projectName: String, file: File): Project =
  Project(projectName, file)
    .settings(
      name := prefixedProjectName(projectName),
      libraryDependencies ++= libs.hedgehogLibs,
      testFrameworks ~= (testFws => (TestFramework("hedgehog.sbt.Framework") +: testFws).distinct),
      licenses := List("MIT" -> url("http://opensource.org/licenses/MIT")),
    )
