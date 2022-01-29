package dev.svejcar.sjq.app

import dev.svejcar.sjq.model.Node
import dev.svejcar.sjq.service.{Emitter, Sanitizer}
import zio.*

object TestApp extends ZIOAppDefault {
  override def run = myApp

  def app =
    for
      emitter <- ZIO.service[Emitter]
      foo     <- emitter.emit(Node.NString())
      _       <- Console.printLine(foo)
    yield ()

  def myApp = app.provide(
    Console.live,
    Emitter.live,
    Sanitizer.live
  )
}
