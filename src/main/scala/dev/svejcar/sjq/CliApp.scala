package dev.svejcar.sjq

import caseapp._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Try

case class Args(@ExtraName("a") access: String, @ExtraName("j") json: Option[String] = None)

object CliApp extends CaseApp[Args] {

  private def readIn =
    Try(Await.result(Future(scala.io.StdIn.readLine()), 10.millis)).toEither

  override def run(options: Args, remainingArgs: RemainingArgs): Unit = {
    import dev.svejcar.sjq.core.Executor._
    import dev.svejcar.sjq.core.Parser._
    import dev.svejcar.sjq.core.Renderer.ops._

    val result = for {
      rawJson <- options.json
        .fold(readIn)(Right(_))
        .left
        .map(_ => "Either --json argument or JSON on stdin is required.")
      json <- io.circe.parser.parse(rawJson).left.map(_.message)
      definitions     = parseDefinitions(json).renderCode
      executionResult = executeCode(json, generateCode(options.access, definitions))
    } yield executionResult

    println(result.fold(identity, identity))
    System.exit(if (result.isLeft) 1 else 0)
  }

}
