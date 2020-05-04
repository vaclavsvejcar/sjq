package dev.svejcar.sjq.core

import dev.svejcar.sjq.core.Parser.parseDefinitions
import org.scalatest.funspec.AnyFunSpec

class ParserSpec extends AnyFunSpec {

  describe("Parser") {
    it("should successfully generate definitions from raw JSON") {
      val json   = io.circe.parser.parse(RawJson).toOption.get
      val parsed = parseDefinitions(json)

      assert(parsed == ParsedDefinitions)
    }
  }

}
