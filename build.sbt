ThisBuild / version      := "0.2.0-SNAPSHOT"
ThisBuild / organization := "dev.svejcar"
ThisBuild / licenses += ("BSD-3-Clause", url("https://opensource.org/licenses/BSD-3-Clause"))
ThisBuild / homepage := Some(url("https://github.com/vaclavsvejcar/sjq"))

ThisBuild / scalaVersion := Dependencies.Version.scala

lazy val root = (project in file("."))
  .settings(
    name := "sjq",
    libraryDependencies ++= Seq(
      Dependencies.ammonite,
      Dependencies.circeCore,
      Dependencies.circeParser,
      Dependencies.zio,
      Dependencies.zioInteropCats,
      Dependencies.zioTest,
      Dependencies.zioTestSbt
    ),
    excludeDependencies ++= Seq(
      ExclusionRule("com.lihaoyi", "sourcecode_2.13"),
      ExclusionRule("com.lihaoyi", "fansi_2.13"),
    ),
    assembly / mainClass := Some("dev.svejcar.sjq.Launcher"),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
