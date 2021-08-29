ThisBuild / scalaVersion := props.ScalaVersion
ThisBuild / version      := props.ProjectVersion
ThisBuild / organization := props.Org
ThisBuild / developers   := List(
  Developer(
    props.GitHubUsername,
    "Kevin Lee",
    "kevin.code@kevinlee.io",
    url(s"https://github.com/${props.GitHubUsername}"),
  ),
)
ThisBuild / homepage     := url(s"https://github.com/${props.GitHubUsername}/${props.RepoName}").some
ThisBuild / scmInfo      :=
  ScmInfo(
    url(s"https://github.com/${props.GitHubUsername}/${props.RepoName}"),
    s"https://github.com/${props.GitHubUsername}/${props.RepoName}.git",
  ).some
ThisBuild / licenses     := List("MIT" -> url("http://opensource.org/licenses/MIT"))

lazy val whatsub = (project in file("."))
  .settings(
    name := props.ProjectName,
  )
  .settings(noPublish)
  .aggregate(core, cli)

lazy val core = subProject("core", file("core"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
//    resolvers += Resolver.sonatypeRepo("snapshots"),
    libraryDependencies ++=
      libs.catsAndCatsEffect3 ++ List(libs.catsParse, libs.effectieCatsEffect3) ++ List(libs.extrasCats),
    /* Build Info { */
    buildInfoKeys    := List[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoObject  := "WhatsubBuildInfo",
    buildInfoPackage := "whatsub.info",
    buildInfoOptions += BuildInfoOption.ToJson,
    /* } Build Info */
  )

lazy val pirate = ProjectRef(props.pirateUri, "pirate")

lazy val cli = subProject("cli", file("cli"))
  .enablePlugins(JavaAppPackaging, NativeImagePlugin)
  .settings(
    maintainer           := "Kevin Lee <kevin.code@kevinlee.io>",
    packageSummary       := "Whatsub - subtitle converter and syncer",
    packageDescription   := "A tool to convert and sync subtitles",
    executableScriptName := props.ExecutableScriptName,
    nativeImageOptions ++= List(
      "--verbose",
      "--no-fallback",
      "-H:+ReportExceptionStackTraces",
      "--initialize-at-build-time",
//      s"-H:ReflectionConfigurationFiles=${ (baseDirectory.value / "graal" / "reflect-config.json").getCanonicalPath }",
//      "--allow-incomplete-classpath",
//      "--report-unsupported-elements-at-runtime",
    ),
  )
  .settings(noPublish)
  .dependsOn(core, pirate)

lazy val props =
  new {
    final val ScalaVersion = "3.0.1"
    final val Org          = "io.kevinlee"

    final val GitHubUsername = "Kevin-Lee"
    final val RepoName       = "whatsub"
    final val ProjectName    = RepoName
    final val ProjectVersion = "0.1.0"

    final val ExecutableScriptName = RepoName

    final val HedgehogVersion = "0.7.0"

    final val CatsVersion        = "2.6.1"
    final val CatsEffect3Version = "3.2.5"

    final val CatsParseVersion = "0.3.4"

    final val EffectieCatsEffect3Version = "1.15.0"

    final val pirateVersion = "main"
    final val pirateUri     = uri(s"https://github.com/$GitHubUsername/pirate.git#$pirateVersion")

    final val IncludeTest: String = "compile->compile;test->test"

    final val ExtrasVersion = "0.1.0"

    final val CanEqualVersion = "0.1.0"

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

    lazy val effectieCatsEffect3 = "io.kevinlee" %% "effectie-cats-effect3" % props.EffectieCatsEffect3Version

    lazy val extrasCats = "io.kevinlee" %% "extras-cats" % props.ExtrasVersion

    lazy val canEqual = "io.kevinlee" %% "can-equal" % props.CanEqualVersion

  }

// format: off
def prefixedProjectName(name: String): String = s"${props.RepoName}${if (name.isEmpty) "" else s"-$name"}"
// format: on

def subProject(projectName: String, file: File): Project =
  Project(projectName, file)
    .settings(
      name                       := prefixedProjectName(projectName),
      useAggressiveScalacOptions := true,
      libraryDependencies ++= libs.hedgehogLibs ++ List(libs.canEqual),
      testFrameworks ~= (testFws => (TestFramework("hedgehog.sbt.Framework") +: testFws).distinct),
      licenses                   := List("MIT" -> url("http://opensource.org/licenses/MIT")),
    )
