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

ThisBuild / resolvers += "sonatype-snapshots" at s"https://${props.SonatypeCredentialHost}/content/repositories/snapshots"

lazy val whatsub = (project in file("."))
  .enablePlugins(DevOopsGitHubReleasePlugin, DocusaurPlugin)
  .settings(
    name                     := props.ProjectName,
    /* GitHub Release { */
    devOopsPackagedArtifacts := List(
      s"cli/target/universal/${name.value}*.zip",
      s"cli/target/native-image/${props.RepoName}-cli-*",
    ),
    /* } GitHub Release */
    docusaurDir              := (ThisBuild / baseDirectory).value / "website",
    docusaurBuildDir         := docusaurDir.value / "build",
  )
  .settings(noPublish)
  .aggregate(core, cli)

lazy val core = subProject("core", file("core"))
  .enablePlugins(BuildInfoPlugin)
  .settings(
//    resolvers += Resolver.sonatypeRepo("snapshots"),
    libraryDependencies ++=
      libs.catsAndCatsEffect3 ++ List(libs.catsParse, libs.effectieCatsEffect3) ++ List(
        libs.extrasCats,
        libs.extrasScalaIo,
        libs.extrasHedgehogCatsEffect3,
      ),
    /* Build Info { */
    buildInfoKeys    := List[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoObject  := "WhatsubBuildInfo",
    buildInfoPackage := "whatsub.info",
    buildInfoOptions += BuildInfoOption.ToJson,
    /* } Build Info */
  )

lazy val pirateScalaz = ProjectRef(props.pirateUri, "pirate-scalaz")

lazy val cli = subProject("cli", file("cli"))
  .enablePlugins(JavaAppPackaging, NativeImagePlugin)
  .settings(
    scalacOptions ++= List("-source:3.1"),
    maintainer           := "Kevin Lee <kevin.code@kevinlee.io>",
    packageSummary       := "Whatsub - subtitle converter and syncer",
    packageDescription   := "A tool to convert and sync subtitles",
    executableScriptName := props.ExecutableScriptName,
    nativeImageVersion   := "22.1.0",
    nativeImageJvm       := "graalvm-java17",
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
  .dependsOn(core, pirateScalaz)

lazy val props =
  new {
    final val ScalaVersion = "3.1.2"
    final val Org          = "io.kevinlee"

    private val gitHubRepo = findRepoOrgAndName

    val GitHubUsername = gitHubRepo.fold("Kevin-Lee")(_.orgToString)
    val RepoName       = gitHubRepo.fold("whatsub")(_.nameToString)

    final val ProjectName = RepoName

    final val ProjectVersion = "1.0.4"

    final val ExecutableScriptName = RepoName

    final val SonatypeCredentialHost = "s01.oss.sonatype.org"

    final val HedgehogVersion = "0.9.0"

    final val CatsVersion        = "2.7.0"
    final val CatsEffect3Version = "3.3.12"

    final val CatsParseVersion = "0.3.7"

    final val EffectieCatsEffect3Version = "2.0.0-beta1"

    final val pirateVersion = "18dfbbca014ba2312a640cf558ab6eca19c47eb8"
    final val pirateUri     = uri(s"https://github.com/$GitHubUsername/pirate.git#$pirateVersion")

    final val IncludeTest: String = "compile->compile;test->test"

    final val ExtrasVersion = "0.15.0"

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

    lazy val extrasCats    = "io.kevinlee" %% "extras-cats"     % props.ExtrasVersion
    lazy val extrasScalaIo = "io.kevinlee" %% "extras-scala-io" % props.ExtrasVersion

    lazy val extrasHedgehogCatsEffect3 = "io.kevinlee" %% "extras-hedgehog-cats-effect3" % props.ExtrasVersion % Test

  }

// format: off
def prefixedProjectName(name: String): String = s"${props.RepoName}${if (name.isEmpty) "" else s"-$name"}"
// format: on

def subProject(projectName: String, file: File): Project =
  Project(projectName, file)
    .settings(
      name                       := prefixedProjectName(projectName),
      useAggressiveScalacOptions := true,
      libraryDependencies ++= libs.hedgehogLibs,
      wartremoverErrors ++= ProjectInfo.commonWarts((update / scalaBinaryVersion).value),
      wartremoverExcluded ++= List(sourceManaged.value),
      testFrameworks ~= (testFws => (TestFramework("hedgehog.sbt.Framework") +: testFws).distinct),
      licenses                   := List("MIT" -> url("http://opensource.org/licenses/MIT")),
    )
