import sbt._

object Dependencies {

  object Version {
    val circe          = "0.14.1"
    val zio            = "2.0.0-RC2"
    val zioInteropCats = "3.2.9.0"
  }

  val circeCore      = "io.circe"      %% "circe-core"          % Version.circe
  val circeParser      = "io.circe"      %% "circe-parser"          % Version.circe
  val zio            = "dev.zio"       %% "zio"                 % Version.zio
  val zioInteropCats = "dev.zio"       %% "zio-interop-cats"    % Version.zioInteropCats
  val zioTest        = "dev.zio"       %% "zio-test"            % Version.zio % "test"
  val zioTestSbt     = "dev.zio"       %% "zio-test-sbt"        % Version.zio % "test"

}
