package dev.svejcar.sjq.cli

import io.circe.Json

import scala.reflect.runtime._
import scala.tools.reflect.ToolBox

object Executor {

  private val mirror  = universe.runtimeMirror(getClass.getClassLoader)
  private val toolbox = mirror.mkToolBox()

  def executeCode(json: Json, code: String): String =
    toolbox.eval(toolbox.parse(code)).asInstanceOf[Json => String](json)

  def generateCode(operation: String, definitions: String, rootType: String): String =
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
