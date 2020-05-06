package dev.svejcar.sjq.core

import cats.implicits._
import dev.svejcar.sjq.core.Emitter._
import org.scalatest.funspec.AnyFunSpec

class EmitterSpec extends AnyFunSpec with TestData {
  describe("Emitter") {

    describe("emitScala") {
      it("should emit valid code for case class") {
        assert(emitScala(ParsedNode1) == EmittedNode1.some)
      }
    }

  }

}
