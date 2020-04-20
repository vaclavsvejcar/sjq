import sbt._

object Dependencies {

  object Versions {
    val caseApp       = "2.0.0-M16"
    val circe         = "0.13.0"
    val scalaCompiler = "2.13.1"
    val simulacrum    = "1.0.0"
    val uTest         = "0.7.4"
  }

  val caseApp       = "com.github.alexarchambault" %% "case-app"      % Versions.caseApp
  val circeCore     = "io.circe"                   %% "circe-core"    % Versions.circe
  val circeGeneric  = "io.circe"                   %% "circe-generic" % Versions.circe
  val circeParser   = "io.circe"                   %% "circe-parser"  % Versions.circe
  val scalaCompiler = "org.scala-lang"             % "scala-compiler" % Versions.scalaCompiler
  val simulacrum    = "org.typelevel"              %% "simulacrum"    % Versions.simulacrum
  val uTest         = "com.lihaoyi"                %% "utest"         % Versions.uTest % Test

}
