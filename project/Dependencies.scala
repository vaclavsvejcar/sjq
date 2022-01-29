import sbt._

object Dependencies {

  object Version {
    val zio = "2.0.0-RC1"
  }

  val zio = "dev.zio" %% "zio" % Version.zio
}
