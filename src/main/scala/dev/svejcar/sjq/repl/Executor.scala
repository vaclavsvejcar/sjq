package dev.svejcar.sjq.repl

object Executor {

  def executeCode(code: String): Unit = ammonite.Main(predefCode = code).run()

  def generateCode(rawJson: String, definitions: String): String =
    s"""|import io.circe.Json
        |import io.circe.generic.auto._
        |import io.circe._
        |import io.circe.syntax._
        |
        |println("\\n\\n--- Welcome to sjq REPL mode ---")
        |println("Compiling type definitions from input JSON (this may take a while)...")
        |$definitions
        |println("\\n")
        |println("[i] To access the data parsed from JSON use the '_root' variable.")
        |println("[i] To serialize data back to JSON use '.asJson.spaces2'\\n\\n")
        |
        |val _json = io.circe.parser.parse(\"\"\" $rawJson \"\"\").toOption.get
        |val _root = _json.as[root].getOrElse(null)
        |""".stripMargin

}
