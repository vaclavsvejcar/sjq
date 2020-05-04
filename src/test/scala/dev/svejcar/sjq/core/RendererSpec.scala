package dev.svejcar.sjq.core

import dev.svejcar.sjq.core.Renderer.ops._
import org.scalatest.funspec.AnyFunSpec

class RendererSpec extends AnyFunSpec {

  describe("Code renderer") {
    it("should render valid Scala code from parsed definitions") {
      val renderedCode = ParsedDefinitions.renderCode

      assert(renderedCode == RenderedCode)
    }
  }

}
