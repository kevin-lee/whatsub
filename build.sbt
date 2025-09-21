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
  .aggregate(core, ai, cli)

lazy val core = module("core")
  .enablePlugins(BuildInfoPlugin)
  .settings(
    libraryDependencies ++=
      libs.catsAndCatsEffect3 ++ List(libs.catsParse) ++ libs.effectie ++ libs.extras ++
        List(
          libs.tests.extrasHedgehogCe3,
        ),
    /* Build Info { */
    buildInfoKeys := List[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoObject := "WhatsubBuildInfo",
    buildInfoPackage := "whatsub.info",
    buildInfoOptions += BuildInfoOption.ToJson,
    /* } Build Info */
  )

lazy val ai = module("ai")
  .settings(
    libraryDependencies ++= libs.openAi4s ++
      List(
        "com.github.pureconfig" %% "pureconfig-cats-effect" % "0.17.4"  % Test,
        "org.http4s"            %% "http4s-blaze-client"    % "0.23.15" % Test,
      ) ++
      libs.loggerF ++
      List(
        libs.tests.extrasHedgehogCe3,
        libs.tests.extrasTestingToolsEffectie,
        libs.tests.extrasHedgehogCe3,
        libs.logback
      ),
  )
  .dependsOn(core)

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
    final val ScalaVersion = "3.3.6"
    final val Org          = "io.kevinlee"

    private val gitHubRepo = findRepoOrgAndName

    val GitHubUsername = gitHubRepo.fold("kevin-lee")(_.orgToString)
    val RepoName       = gitHubRepo.fold("whatsub")(_.nameToString)

    final val ProjectName = RepoName

    final val ExecutableScriptName = RepoName

    final val HedgehogVersion = "0.10.1"

    final val CatsVersion        = "2.10.0"
    final val CatsEffect3Version = "3.5.1"

    final val CatsParseVersion = "0.3.9"

    final val EffectieVersion = "2.0.0"
    val LoggerFVersion        = "2.1.8"

    final val pirateVersion = "2993d850bf3b92c558bed6d41aa3298217dc87ef"
    final val pirateUri     = uri(s"https://github.com/$GitHubUsername/pirate.git#$pirateVersion")

    final val IncludeTest: String = "compile->compile;test->test"

    final val ExtrasVersion = "0.42.0"

    val OpenAi4sVersion = "0.1.0-alpha13"

    val Refined4sVersion = "1.1.0"

  }

lazy val libs =
  new {

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

    lazy val loggerF = List(
      "io.kevinlee" %% "logger-f-cats"  % props.LoggerFVersion,
      "io.kevinlee" %% "logger-f-slf4j" % props.LoggerFVersion,
    )

    lazy val logback = "ch.qos.logback" % "logback-classic" % "1.4.11"

    lazy val extrasCats    = "io.kevinlee" %% "extras-cats"     % props.ExtrasVersion
    lazy val extrasRender  = "io.kevinlee" %% "extras-render"   % props.ExtrasVersion
    lazy val extrasScalaIo = "io.kevinlee" %% "extras-scala-io" % props.ExtrasVersion

    lazy val openAi4s = List(
      "io.kevinlee" %% "openai4s-core"   % props.OpenAi4sVersion,
      "io.kevinlee" %% "openai4s-config" % props.OpenAi4sVersion,
      "io.kevinlee" %% "openai4s-api"    % props.OpenAi4sVersion,
      "io.kevinlee" %% "openai4s-http4s" % props.OpenAi4sVersion,
    )

    lazy val extras = List(
      extrasCats,
      extrasRender,
      extrasScalaIo,
    )

    lazy val refined4sCore         = "io.kevinlee" %% "refined4s-core"          % props.Refined4sVersion
    lazy val refined4sCats         = "io.kevinlee" %% "refined4s-cats"          % props.Refined4sVersion
    lazy val refined4sChimney      = "io.kevinlee" %% "refined4s-chimney"       % props.Refined4sVersion
    lazy val refined4sExtrasRender = "io.kevinlee" %% "refined4s-extras-render" % props.Refined4sVersion

    lazy val tests = new {

      lazy val hedgehogLibs = List(
        "qa.hedgehog" %% "hedgehog-core"   % props.HedgehogVersion % Test,
        "qa.hedgehog" %% "hedgehog-runner" % props.HedgehogVersion % Test,
        "qa.hedgehog" %% "hedgehog-sbt"    % props.HedgehogVersion % Test,
      )

      lazy val extrasTestingToolsCats     = "io.kevinlee" %% "extras-testing-tools-cats" % props.ExtrasVersion % Test
      lazy val extrasTestingToolsEffectie =
        "io.kevinlee" %% "extras-testing-tools-effectie" % props.ExtrasVersion % Test
      lazy val extrasHedgehogCe3 = "io.kevinlee" %% "extras-hedgehog-ce3" % props.ExtrasVersion % Test

    }

  }

// format: off
def prefixedProjectName(name: String): String = s"${props.RepoName}${if (name.isEmpty) "" else s"-$name"}"
// format: on

def module(projectName: String): Project = {
  val prefixedName = prefixedProjectName(projectName)
  Project(projectName, file(s"modules/$prefixedName"))
    .settings(
      name := prefixedName,
//      useAggressiveScalacOptions := true,
      //      scalacOptions ++= List("-source:3.1", "-Yexplicit-nulls"),
      scalacOptions ++= List("-source:3.3"),
//      scalacOptions ~= (existing =>
//        existing.filter(
//          _ != "-language:dynamics,existentials,higherKinds,reflectiveCalls,experimental.macros,implicitConversions,strictEquality"
//        )
//      ),
      scalacOptions := (
        scalacOptions.value ++ List(
          "-language:dynamics",
          "-language:existentials",
          "-language:higherKinds",
          "-language:reflectiveCalls",
          "-language:experimental.macros",
          "-language:implicitConversions",
          "-language:strictEquality"
        )
      ).distinct,
      libraryDependencies ++= libs.tests.hedgehogLibs,
      wartremoverErrors ++= ProjectInfo.commonWarts((update / scalaBinaryVersion).value),
      wartremoverExcluded ++= List(sourceManaged.value),
      licenses := List("MIT" -> url("http://opensource.org/licenses/MIT")),
    )
}
