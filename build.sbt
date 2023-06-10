ThisBuild / scalaVersion := props.ScalaVersion
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

ThisBuild / resolvers += "sonatype-snapshots" at s"https://${props.SonatypeCredentialHost}/content/repositories/snapshots"

lazy val whatsub = (project in file("."))
  .enablePlugins(DevOopsGitHubReleasePlugin, DocusaurPlugin)
  .settings(
    name := props.ProjectName,
    /* GitHub Release { */
    devOopsPackagedArtifacts := List(
      s"modules/${props.ProjectName}-cli/target/universal/${name.value}*.zip",
      s"modules/${props.ProjectName}-cli/target/native-image/${props.RepoName}-cli-*",
    ),
    /* } GitHub Release */
    docusaurDir := (ThisBuild / baseDirectory).value / "website",
    docusaurBuildDir := docusaurDir.value / "build",
  )
  .settings(noPublish)
  .aggregate(core, cli)

lazy val core = module("core")
  .enablePlugins(BuildInfoPlugin)
  .settings(
//    resolvers += Resolver.sonatypeRepo("snapshots"),
    libraryDependencies ++=
      libs.catsAndCatsEffect3 ++ List(libs.catsParse) ++ libs.effectie ++ libs.extras,
    /* Build Info { */
    buildInfoKeys := List[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoObject := "WhatsubBuildInfo",
    buildInfoPackage := "whatsub.info",
    buildInfoOptions += BuildInfoOption.ToJson,
    /* } Build Info */
  )

lazy val pirateScalaz = ProjectRef(props.pirateUri, "pirate-scalaz")

lazy val cli = module("cli")
  .enablePlugins(JavaAppPackaging, NativeImagePlugin)
  .settings(
    maintainer := "Kevin Lee <kevin.code@kevinlee.io>",
    packageSummary := "Whatsub - subtitle converter and syncer",
    packageDescription := "A tool to convert and sync subtitles",
    executableScriptName := props.ExecutableScriptName,
    nativeImageVersion := "22.3.0",
    nativeImageJvm := "graalvm-java17",
    nativeImageOptions ++= List(
      "--verbose",
      "--no-fallback",
      "-H:+ReportExceptionStackTraces",
      "--initialize-at-build-time",
      "-H:+AddAllCharsets",
//      s"-H:ReflectionConfigurationFiles=${ (baseDirectory.value / "graal" / "reflect-config.json").getCanonicalPath }",
//      "--allow-incomplete-classpath",
      "--report-unsupported-elements-at-runtime",
    ),
  )
  .settings(noPublish)
  .dependsOn(
    core % props.IncludeTest,
    pirateScalaz
  )

lazy val props =
  new {
    final val ScalaVersion = "3.3.0"
    final val Org          = "io.kevinlee"

    private val gitHubRepo = findRepoOrgAndName

    val GitHubUsername = gitHubRepo.fold("kevin-lee")(_.orgToString)
    val RepoName       = gitHubRepo.fold("whatsub")(_.nameToString)

    final val ProjectName = RepoName

    final val ExecutableScriptName = RepoName

    final val SonatypeCredentialHost = "s01.oss.sonatype.org"

    final val HedgehogVersion = "0.10.1"

    final val CatsVersion        = "2.9.0"
    final val CatsEffect3Version = "3.4.8"

    final val CatsParseVersion = "0.3.9"

    final val EffectieVersion = "2.0.0-beta9"

    final val pirateVersion = "7797fb3884bdfdda7751d8f75accf622b30a53ed"
    final val pirateUri     = uri(s"https://github.com/$GitHubUsername/pirate.git#$pirateVersion")

    final val IncludeTest: String = "compile->compile;test->test"

    final val ExtrasVersion = "0.38.0"

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

    lazy val effectie = List(
      "io.kevinlee" %% "effectie-core"         % props.EffectieVersion,
      "io.kevinlee" %% "effectie-syntax"       % props.EffectieVersion,
      "io.kevinlee" %% "effectie-cats-effect3" % props.EffectieVersion,
    )

    lazy val extrasCats    = "io.kevinlee" %% "extras-cats"     % props.ExtrasVersion
    lazy val extrasScalaIo = "io.kevinlee" %% "extras-scala-io" % props.ExtrasVersion

    lazy val extrasHedgehogCe3 = "io.kevinlee" %% "extras-hedgehog-ce3" % props.ExtrasVersion % Test

    lazy val extras = List(
      extrasCats,
      extrasScalaIo,
      extrasHedgehogCe3
    )

  }

// format: off
def prefixedProjectName(name: String): String = s"${props.RepoName}${if (name.isEmpty) "" else s"-$name"}"
// format: on

def module(projectName: String): Project = {
  val prefixedName = prefixedProjectName(projectName)
  Project(projectName, file(s"modules/$prefixedName"))
    .settings(
      name := prefixedName,
      useAggressiveScalacOptions := true,
      //      scalacOptions ++= List("-source:3.1", "-Yexplicit-nulls"),
      scalacOptions ++= List("-source:3.2"),
      scalacOptions ~= (existing =>
        existing.filter(
          _ != "-language:dynamics,existentials,higherKinds,reflectiveCalls,experimental.macros,implicitConversions,strictEquality"
        )
      ),
      libraryDependencies ++= libs.hedgehogLibs,
      wartremoverErrors ++= ProjectInfo.commonWarts((update / scalaBinaryVersion).value),
      wartremoverExcluded ++= List(sourceManaged.value),
      testFrameworks ~= (testFws => (TestFramework("hedgehog.sbt.Framework") +: testFws).distinct),
      licenses := List("MIT" -> url("http://opensource.org/licenses/MIT")),
    )
}
