package dev.svejcar.sjq

import dev.svejcar.sjq.core.Emitter._
import dev.svejcar.sjq.core.Parser._
import optparse_applicative.execParser

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.util.{Success, Try, Using}

object Launcher {

  def main(args: Array[String]): Unit =
    execParser(args, "sjq", Options.parser) match {
      case opts: Options.Cli  => runCli(opts)
      case opts: Options.Repl => runRepl(opts)
    }

  private def runCli(opts: Options.Cli): Unit = {
    import dev.svejcar.sjq.cli.Executor._

    val result = for {
      rawJson <- getJson(opts.json, "Either --json argument or JSON on stdin is required.")
      json    <- io.circe.parser.parse(rawJson).left.map(_.message)
      repr            = parseJson(json)
      code            = emit(repr)
      executionResult = executeCode(json, generateCode(opts.access, code.getOrElse(""), emitType(repr, RootType, None)))
    } yield executionResult

    println(result.fold(identity, identity))
    System.exit(if (result.isLeft) 1 else 0)
  }

  private def runRepl(opts: Options.Repl): Unit = {
    import dev.svejcar.sjq.repl.Executor._

    val getRawJson = (opts.input match {
      case SourceType.Inline(input)   => Success(input)
      case SourceType.LocalFile(file) => Using(Source.fromFile(file))(_.getLines().mkString)
    }).toEither.left.map(_.getMessage)

    val result = for {
      rawJson <- getRawJson
      json    <- io.circe.parser.parse(rawJson).left.map(_.message)
      repr = parseJson(json)
      code = generateCode(emit(repr).getOrElse(""), emitType(repr, RootType, None))
    } yield executeCode(code, repr, json)

    result.left.map(println)
    System.exit(if (result.isLeft) 1 else 0)
  }

  def getJson(rawJson: Option[String], msg: String): Either[String, String] =
    rawJson.fold(readIn)(Right(_)).left.map(_ => msg)

  private def readIn: Either[Throwable, String] =
    Try(
      Await.result(Future(LazyList.continually(scala.io.StdIn.readLine()).takeWhile(_ != null).mkString), 10.millis)
    ).toEither

}
