package dev.svejcar.sjq.core

import utest._

object ParserSuite extends TestSuite {
  import Parser._

  override def tests: Tests = Tests {

    test("Code parser should successfully generate definitions from raw JSON") {
      val json   = io.circe.parser.parse(RawJson).toOption.get
      val parsed = parseDefinitions(json)

      assert(parsed == ParsedDefinitions)
    }
  }
}
