package dev.svejcar.sjq.core

import utest._

import scala.io.Source

object IntegrationSuite extends TestSuite {
  import dev.svejcar.sjq.cli.Executor._
  import Parser._
  import Renderer.ops._

  override def tests: Tests = Tests {
    test("sjq should return unchanged JSON when no modifications are made") {
      val exampleJson = io.circe.parser.parse(Source.fromResource("example.json").getLines.mkString).toOption.get

      val result = executeCode(exampleJson, generateCode("_root", parseDefinitions(exampleJson).renderCode))
      val actual = io.circe.parser.parse(result).toOption.get

      assert(actual == exampleJson)
    }
  }
}
