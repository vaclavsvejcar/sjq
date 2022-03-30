/*
 * sjq :: Command-line JSON processor
 * Copyright (c) 2020-2022 Vaclav Svejcar
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

import dev.svejcar.sjq.service._
import optparse_applicative.execParser
import zio.{Chunk, Console, ZIO, ZIOAppArgs, ZIOAppDefault}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.io.Source
import scala.util.Using

object Launcher extends ZIOAppDefault {

  override def run = {
    def chooseApp(options: Options) = options match {
      case opts: Options.Cli  => runCli(opts)
      case opts: Options.Repl => runRepl(opts)
    }

    for {
      args    <- ZIOAppArgs.getArgs
      options <- parseOptions(args)
      _ <- chooseApp(options).provide(Cli.live, Console.live, Emitter.live, Parser.live, Repl.live, Sanitizer.live)
    } yield ()
  }

  private def runRepl(opts: Options.Repl) = {

    val getRawJson = opts.input match {
      case SourceType.Inline(input)   => ZIO.succeed(input)
      case SourceType.LocalFile(path) => ZIO.fromTry(Using(Source.fromFile(path))(_.getLines().mkString))
    }

    for {
      rawJson <- getRawJson
      json    <- parseJson(rawJson)
      repr    <- Parser.parseJson(json)
      defs    <- Emitter.emit(repr).map(_.getOrElse(""))
      root    <- Emitter.emitRoot(repr)
      code    <- Repl.generateCode(defs, root)
      _       <- Repl.executeCode(code, defs, repr, json)
    } yield ()
  }

  private def runCli(opts: Options.Cli) = {
    for {
      rawJson <- readIn
      json    <- parseJson(rawJson)
      repr    <- Parser.parseJson(json)
      defs    <- Emitter.emit(repr).map(_.getOrElse(""))
      root    <- Emitter.emitRoot(repr)
      code    <- Cli.generateCode(opts.access, defs, root)
      result  <- Cli.executeCode(json, code)
      _       <- Console.printLine(result)
    } yield ()
  }

  // TODO use ZIO stuff here
  private def readIn = {
    ZIO.from {
      Await.result(Future(LazyList.continually(scala.io.StdIn.readLine()).takeWhile(_ != null).mkString), 10.millis)
    }
  }

  private def parseJson(raw: String) = ZIO.fromEither(io.circe.parser.parse(raw))

  private def parseOptions(args: Chunk[String]) =
    ZIO.succeed(execParser(args.toArray, BuildInfo.name, Options.parser));
}
