package dev.svejcar.sjq

import cats.implicits._

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

case class Options(mode: RunMode = RunMode.Other, access: String = "", json: Option[String] = None)

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
          opt[String]('j', "json").action((x, c) => c.copy(json = x.some))
        ),
      cmd("repl")
        .action((_, c) => c.copy(mode = RunMode.Repl))
        .text("runs interactive mode using the Ammonite REPL")
        .children(
          opt[String]('j', "json").action((x, c) => c.copy(json = x.some))
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
    case RunMode.Cli => runCli(options.access, options.json)
    case _           => throw new UnsupportedOperationException("not implemented yet")
  }

  private def runCli(access: String, json: Option[String]): Unit = {
    import dev.svejcar.sjq.core.Executor._
    import dev.svejcar.sjq.core.Parser._
    import dev.svejcar.sjq.core.Renderer.ops._

    def readIn =
      Try(Await.result(Future(scala.io.StdIn.readLine()), 10.millis)).toEither

    val result = for {
      rawJson <- json
        .fold(readIn)(Right(_))
        .left
        .map(_ => "Either --json argument or JSON on stdin is required.")
      json <- io.circe.parser.parse(rawJson).left.map(_.message)
      definitions     = parseDefinitions(json).renderCode
      executionResult = executeCode(json, generateCode(access, definitions))
    } yield executionResult

    println(result.fold(identity, identity))
    System.exit(if (result.isLeft) 1 else 0)
  }
}
