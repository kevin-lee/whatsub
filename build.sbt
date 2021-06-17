ThisBuild / scalaVersion := props.ScalaVersion
ThisBuild / version := props.ProjectVersion
ThisBuild / organization := props.Org
ThisBuild / organizationName := props.OrgName
ThisBuild / developers := List(
  Developer(
    props.GitHubUsername,
    "Kevin Lee",
    "kevin.code@kevinlee.io",
    url(s"https://github.com/${props.GitHubUsername}"),
  )
)
ThisBuild / homepage := url(s"https://github.com/${props.GitHubUsername}/${props.RepoName}").some
ThisBuild / scmInfo :=
  ScmInfo(
    url(s"https://github.com/${props.GitHubUsername}/${props.RepoName}"),
    s"https://github.com/${props.GitHubUsername}/${props.RepoName}.git",
  ).some

lazy val justPoi = (project in file("."))
  .settings(
    name := props.ProjectName
  )
  .settings(noPublish)
  .aggregate(core)

lazy val core = subProject("core", file("core"))
  .settings(
    libraryDependencies ++=
      libs.refined ++ libs.catsAndCatsEffect2,
  )

lazy val props =
  new {
    final val ScalaVersion = "3.0.0"
    final val Org          = "io.kevinlee"
    final val OrgName      = ""

    final val GitHubUsername = "Kevin-Lee"
    final val RepoName       = "whatsub"
    final val ProjectName    = "whatsub"
    final val ProjectVersion = "0.1.0-SNAPSHOT"

    final val refinedVersion = "0.9.26"

    final val hedgehogVersion = "0.7.0"

    final val catsVersion        = "2.6.1"
    final val catsEffect2Version = "2.5.1"

    final val IncludeTest: String = "compile->compile;test->test"
  }

lazy val libs =
  new {
    lazy val hedgehogLibs = List(
      "qa.hedgehog" %% "hedgehog-core"   % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-runner" % props.hedgehogVersion % Test,
      "qa.hedgehog" %% "hedgehog-sbt"    % props.hedgehogVersion % Test,
    )

    lazy val refined = List(
      "eu.timepit" %% "refined" % props.refinedVersion
//      "eu.timepit" %% "refined-cats" % props.refinedVersion,
    )

    lazy val catsAndCatsEffect2 = List(
      "org.typelevel" %% "cats-core"   % props.catsVersion,
      "org.typelevel" %% "cats-effect" % props.catsEffect2Version,
    )

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
    )
