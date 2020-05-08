/*
 * sjq :: Command-line JSON processor
 * Copyright (c) 2020 Vaclav Svejcar
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
