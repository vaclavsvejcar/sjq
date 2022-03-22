import sbt._

object Dependencies {

  object Version {
    val ammonite       = "2.5.2"
    val circe          = "0.14.1"
    val optparse       = "0.9.2"
    val zio            = "2.0.0-RC3"
    val zioInteropCats = "3.2.9.1"
    val scala          = "2.13.8"
  }

  val ammonite       = "com.lihaoyi"         % "ammonite"             % Version.ammonite cross CrossVersion.full
  val circeCore      = "io.circe"           %% "circe-core"           % Version.circe
  val circeParser    = "io.circe"           %% "circe-parser"         % Version.circe
  val optparse       = "com.github.xuwei-k" %% "optparse-applicative" % Version.optparse
  val scalaCompiler  = "org.scala-lang"      % "scala-compiler"       % Version.scala
  val zio            = "dev.zio"            %% "zio"                  % Version.zio
  val zioInteropCats = "dev.zio"            %% "zio-interop-cats"     % Version.zioInteropCats
  val zioTest        = "dev.zio"            %% "zio-test"             % Version.zio % "test"
  val zioTestSbt     = "dev.zio"            %% "zio-test-sbt"         % Version.zio % "test"

}
