ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "dev.svejcar"
ThisBuild / scalaVersion := "2.13.1"
ThisBuild / licenses += ("BSD-3-Clause", url("https://opensource.org/licenses/BSD-3-Clause"))
ThisBuild / homepage := Some(url("https://github.com/vaclavsvejcar/sjq"))

lazy val root = (project in file("."))
  .settings(
    name := "sjq",
    libraryDependencies ++= Seq(
      Dependencies.ammoniteRepl,
      Dependencies.circeCore,
      Dependencies.circeGeneric,
      Dependencies.circeParser,
      Dependencies.scalaCompiler,
      Dependencies.scopt,
      Dependencies.simulacrum,
      Dependencies.uTest
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    assembly / mainClass := Some("dev.svejcar.sjq.Launcher"),
    assembly / test := {},
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x                             => MergeStrategy.first
    },
    Test / parallelExecution := false
  )

ThisBuild / scalacOptions ++= Seq(
  "-feature",
  "-language:implicitConversions",
  "-Ymacro-annotations"
)
