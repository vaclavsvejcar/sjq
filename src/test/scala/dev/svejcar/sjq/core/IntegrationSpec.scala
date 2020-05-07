package dev.svejcar.sjq.core

import dev.svejcar.sjq.cli.Executor._
import dev.svejcar.sjq.core.Emitter._
import dev.svejcar.sjq.core.Parser._
import org.scalatest.funspec.AnyFunSpec

import scala.io.Source

class IntegrationSpec extends AnyFunSpec {

  describe("sjq integration test") {
    it("should return unchanged JSON when no modifications are made") {
      val exampleJson = io.circe.parser.parse(Source.fromResource("example.json").getLines.mkString).toOption.get

      val result = executeCode(exampleJson, generateCode("root", emit(parseJson(exampleJson)).get, RootType))
      val actual = io.circe.parser.parse(result).toOption.get

      assert(actual == exampleJson)
    }
  }

}
