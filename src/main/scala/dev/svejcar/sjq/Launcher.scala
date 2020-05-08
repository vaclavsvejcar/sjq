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
    execParser(args, BuildInfo.name, Options.parser) match {
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
