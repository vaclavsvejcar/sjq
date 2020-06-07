import sbt._

object Dependencies {

  object Versions {
    val ammonite      = "2.1.4"
    val circe         = "0.13.0"
    val optparse      = "0.9.0"
    val scalaCompiler = "2.13.2"
    val scalaTest     = "3.1.2"
    val simulacrum    = "1.0.0"
  }

  val ammoniteRepl  = "com.lihaoyi"        % "ammonite"              % Versions.ammonite cross CrossVersion.full
  val circeCore     = "io.circe"           %% "circe-core"           % Versions.circe
  val circeGeneric  = "io.circe"           %% "circe-generic"        % Versions.circe
  val circeParser   = "io.circe"           %% "circe-parser"         % Versions.circe
  val optparse      = "com.github.xuwei-k" %% "optparse-applicative" % Versions.optparse
  val scalaCompiler = "org.scala-lang"     % "scala-compiler"        % Versions.scalaCompiler
  val scalaTest     = "org.scalatest"      %% "scalatest"            % Versions.scalaTest % "test"
  val simulacrum    = "org.typelevel"      %% "simulacrum"           % Versions.simulacrum
}
