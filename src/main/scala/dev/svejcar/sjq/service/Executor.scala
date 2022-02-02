package dev.svejcar.sjq.service

import io.circe.{Json, JsonObject}
import zio.*

trait Executor:
  def executeCode(json: Json, code: String): UIO[String]
  def generateCode(operation: String, definitions: String, rootType: String): UIO[String]

object Executor:
  val live: ULayer[ExecutorLive] = ZLayer.succeed(ExecutorLive())

case class ExecutorLive() extends Executor:

  override def executeCode(json: Json, code: String): UIO[String] =
    // FIXME figure out how to do compilation in runtime in Scala 3
    ???

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
