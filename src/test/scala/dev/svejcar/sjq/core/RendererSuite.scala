package dev.svejcar.sjq.core

import utest._

object RendererSuite extends TestSuite {
  import Renderer.ops._

  override def tests: Tests = Tests {

    test("Code renderer should render valid Scala code from parsed definitions") {
      val renderedCode = ParsedDefinitions.renderCode

      assert(renderedCode == RenderedCode)
    }
  }
}
