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

package dev.svejcar.sjq.service

import io.circe.Json
import zio._

import scala.reflect.runtime._
import scala.tools.reflect.ToolBox

trait Cli {
  def executeCode(json: Json, code: String): Task[String]
  def generateCode(operation: String, definitions: String, rootType: String): UIO[String]
}

object Cli {
  val live: ULayer[CliLive] = ZLayer.succeed(CliLive())

  def executeCode(json: Json, code: String): ZIO[Cli, Throwable, String] =
    ZIO.serviceWithZIO[Cli](_.executeCode(json, code))

  def generateCode(operation: String, definitions: String, rootType: String): ZIO[Cli, Nothing, String] =
    ZIO.serviceWithZIO[Cli](_.generateCode(operation, definitions, rootType))
}

case class CliLive() extends Cli {
  private val mirror  = universe.runtimeMirror(getClass.getClassLoader)
  private val toolbox = mirror.mkToolBox()

  override def executeCode(json: Json, code: String): Task[String] =
    Task(toolbox.eval(toolbox.parse(code)).asInstanceOf[Json => String](json))

  override def generateCode(operation: String, definitions: String, rootType: String): UIO[String] =
    ZIO.succeed {
      s"""
         |import io.circe.Json
         |
         |(json: Json) => {
         |  import io.circe.generic.auto._
         |  import io.circe._
         |  import io.circe.syntax._
         |
         |  $definitions
         |
         |  val root = json.as[$rootType].getOrElse(null)
         |  val result = $operation
         |  result.asJson.spaces2
         |}
     """.stripMargin
    }
}
