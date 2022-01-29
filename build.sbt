ThisBuild / version := "0.2.0-SNAPSHOT"
ThisBuild / organization := "dev.svejcar"
ThisBuild / licenses += ("BSD-3-Clause", url("https://opensource.org/licenses/BSD-3-Clause"))
ThisBuild / homepage := Some(url("https://github.com/vaclavsvejcar/sjq"))

ThisBuild / scalaVersion := "3.1.1"

lazy val root = (project in file("."))
  .settings(
    name := "sjq",
    libraryDependencies ++= Seq(
      Dependencies.zio
    ),
    assembly / mainClass := Some("dev.svejcar.sjq.Launcher")
  )
