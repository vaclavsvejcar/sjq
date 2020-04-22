package dev.svejcar.sjq

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Try

sealed trait RunMode
object RunMode {
  case object Cli   extends RunMode
  case object Repl  extends RunMode
  case object Other extends RunMode
}

sealed trait InputSource
object InputSource {
  case class LocalFile(path: String)  extends InputSource
  case class UrlLocation(url: String) extends InputSource
}

case class Options(mode: RunMode = RunMode.Other, access: String = "", json: String = "")

object Launcher {

  import scopt.OParser

  private val builder = OParser.builder[Options]
  private val parser = {
    import builder._
    OParser.sequence(
      programName("sjq"),
      head("sjq", " v0.1.0"),
      help("help").text("prints this usage text"),
      cmd("cli")
        .action((_, c) => c.copy(mode = RunMode.Cli))
        .text("runs non-interactive mode, similar to original jq")
        .children(
          opt[String]('a', "access").required().action((x, c) => c.copy(access = x)),
          opt[String]('j', "json").action((x, c) => c.copy(json = x))
        ),
      cmd("repl")
        .action((_, c) => c.copy(mode = RunMode.Repl))
        .text("runs interactive mode using the Ammonite REPL")
        .children(
          opt[String]('j', "json").required().action((x, c) => c.copy(json = x))
        )
    )
  }

  def main(args: Array[String]): Unit = {
    OParser.parse(parser, args, Options()) match {
      case Some(options) => run(options)
      case _             =>
    }
  }

  private def run(options: Options): Unit = options.mode match {
    case RunMode.Cli  => runCli(options.access, emptyToNone(options.json))
    case RunMode.Repl => runRepl(emptyToNone(options.json))
    case _            => throw new UnsupportedOperationException("not implemented yet")
  }

  private def runCli(access: String, json: Option[String]): Unit = {
    import dev.svejcar.sjq.cli.Executor._
    import dev.svejcar.sjq.core.Parser._
    import dev.svejcar.sjq.core.Renderer.ops._

    val result = for {
      rawJson <- getJson(json)
      json    <- io.circe.parser.parse(rawJson).left.map(_.message)
      definitions     = parseDefinitions(json).renderCode
      executionResult = executeCode(json, generateCode(access, definitions))
    } yield executionResult

    println(result.fold(identity, identity))
    System.exit(if (result.isLeft) 1 else 0)
  }

  private def runRepl(json: Option[String]): Unit = {
    import dev.svejcar.sjq.core.Parser._
    import dev.svejcar.sjq.core.Renderer.ops._
    import dev.svejcar.sjq.repl.Executor._

    val result = for {
      rawJson <- Try(json.get).toEither
      json    <- io.circe.parser.parse(rawJson).left.map(_.message)
      definitions = parseDefinitions(json).renderCode
    } yield executeCode(generateCode(rawJson, definitions))

    result.left.map(println)
    System.exit(if (result.isLeft) 1 else 0)
  }

  private def getJson(rawJson: Option[String]): Either[String, String] =
    rawJson
      .fold(readIn)(Right(_))
      .left
      .map(_ => "Either --json argument or JSON on stdin is required.")

  private def readIn: Either[Throwable, String] =
    Try(
      Await.result(Future(LazyList.continually(scala.io.StdIn.readLine()).takeWhile(_.nonEmpty).mkString), 100.millis)
    ).toEither

  private def emptyToNone(str: String): Option[String] = Option(str).collect { case x if x.trim.nonEmpty => x }
}
