package dev.svejcar.sjq.core

import dev.svejcar.sjq.core.Parser.parseJson
import org.scalatest.funspec.AnyFunSpec

class ParserSpec extends AnyFunSpec with TestData {

  describe("JSON parser") {
    it("should parse JSON into internal AST representation") {
      val json   = io.circe.parser.parse(RawJson1).toOption.get
      val actual = parseJson(json)

      assert(actual == ParsedNode1)
    }
  }

}
