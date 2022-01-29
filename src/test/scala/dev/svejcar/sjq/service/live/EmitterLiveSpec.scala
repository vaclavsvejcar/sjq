package dev.svejcar.sjq.service.live

import cats.implicits.*
import dev.svejcar.sjq.service.{Emitter, Sanitizer}
import dev.svejcar.sjq.test.TestData
import zio.*
import zio.test.*
import zio.test.Assertion.*

object EmitterLiveSpec extends DefaultRunnableSpec with TestData:

  override def spec = suite("EmitterLiveSpec") {
    test("emit produces valid Scala code") {
      for
        emitter <- ZIO.service[Emitter]
        actual  <- emitter.emit(ParsedNode1)
      yield assert(actual)(equalTo(EmittedNode1.some))
    }
  }.provide(Emitter.live, Sanitizer.live)
