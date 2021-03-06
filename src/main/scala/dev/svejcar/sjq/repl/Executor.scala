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
package dev.svejcar.sjq.repl

import dev.svejcar.sjq.core.Node
import io.circe.Json

object Executor {

  def executeCode(code: String, defs: String, ast: Node, json: Json): Unit =
    ammonite.Main(predefCode = code).run("ast" -> ast, "defs" -> defs, "json" -> json)

  def generateCode(definitions: String, rootType: String): String =
    s"""|import io.circe.Json
        |import io.circe.generic.auto._
        |import io.circe._
        |import io.circe.syntax._
        |
        |println("\\n\\n--- Welcome to sjq REPL mode ---")
        |println("Compiling type definitions from input JSON (this may take a while)...")
        |$definitions
        |println("\\n")
        |println("[i] Variable 'root' holds Scala representation of parsed JSON")
        |println("[i] Variable 'json' holds parsed JSON")
        |println("[i] Variable 'ast' holds internal AST representation of data (for debugging purposes)")
        |println("[i] Variable 'defs' holds generated Scala definitions (for debugging purposes)")
        |println("[i] To serialize data back to JSON use '.asJson.spaces2'\\n\\n")
        |
        |val root = json.as[$rootType].getOrElse(null)
        |""".stripMargin

}
