ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "dev.svejcar"
ThisBuild / scalaVersion := "2.13.2"
ThisBuild / licenses += ("BSD-3-Clause", url("https://opensource.org/licenses/BSD-3-Clause"))
ThisBuild / homepage := Some(url("https://github.com/vaclavsvejcar/sjq"))

lazy val root = (project in file("."))
  .enablePlugins(BuildInfoPlugin)
  .settings(
    name := "sjq",
    libraryDependencies ++= Seq(
      Dependencies.ammoniteRepl,
      Dependencies.circeCore,
      Dependencies.circeGeneric,
      Dependencies.circeParser,
      Dependencies.scalaCompiler,
      Dependencies.scalaTest,
      Dependencies.scopt,
      Dependencies.simulacrum
    ),
    assembly / mainClass := Some("dev.svejcar.sjq.Launcher"),
    assembly / test := {},
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x                             => MergeStrategy.first
    },
    Test / parallelExecution := false,
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "dev.svejcar.sjq"
  )

ThisBuild / scalacOptions ++= Seq(
  "-feature",
  "-language:implicitConversions",
  "-Ymacro-annotations"
)
