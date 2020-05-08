package dev.svejcar.sjq

import java.io.File

import optparse_applicative._
import scalaz.syntax.applicativePlus._

sealed trait SourceType
object SourceType {
  case class Inline(raw: String)   extends SourceType
  case class LocalFile(file: File) extends SourceType
}

sealed trait Options
object Options {
  case class Cli(access: String, json: Option[String]) extends Options
  case class Repl(input: SourceType)                   extends Options

  private val cliOptions: Parser[Options] = ^(
    strOption(short('a'), long("access"), metavar("EXPRESSION"), help("Operation to perform on 'root' object")),
    optional(strOption(short('j'), long("json"), metavar("JSON"), help("JSON to process")))
  )(Cli.apply)

  private val inlineSourceType: Parser[SourceType] =
    strOption(short('j'), long("json"), metavar("JSON"), help("JSON to process")).map(SourceType.Inline)
  private val fileSourceType: Parser[SourceType] =
    strOption(short('f'), long("file"), metavar("FILE"), help("JSON file to process"))
      .map(new File(_))
      .map(SourceType.LocalFile)

  private val replOptions: Parser[Options] = (inlineSourceType <|> fileSourceType).map(Repl.apply)

  private val commands = subparser(
    command("cli", info(cliOptions, progDesc("Non-interactive mode, similar to original jq"))),
    command("repl", info(replOptions, progDesc("Interactive mode using the Ammonite REPL")))
  )

  val parser: ParserInfo[Options] = info(commands <*> helper)

}
