package dev.svejcar.sjq.repl

import dev.svejcar.sjq.core.Node
import io.circe.Json

object Executor {

  def executeCode(code: String, ast: Node, json: Json): Unit =
    ammonite.Main(predefCode = code).run("ast" -> ast, "json" -> json)

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
        |println("[i] To serialize data back to JSON use '.asJson.spaces2'\\n\\n")
        |
        |val root = json.as[$rootType].getOrElse(null)
        |""".stripMargin

}
