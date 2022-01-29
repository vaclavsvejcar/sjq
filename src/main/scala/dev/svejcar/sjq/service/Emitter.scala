package dev.svejcar.sjq.service

import cats.implicits.*
import dev.svejcar.sjq.model.Node
import dev.svejcar.sjq.model.Node.*
import dev.svejcar.sjq.service.{Emitter, Sanitizer}
import zio.*

trait Emitter:
  def emit(node: Node): UIO[Option[String]]

object Emitter:
  val live: URLayer[Sanitizer, dev.svejcar.sjq.service.Emitter] = (EmitterLive(_)).toLayer[Emitter]
  def emit(node: Node): URIO[Emitter, Option[String]]           = ZIO.serviceWithZIO(_.emit(node))

case class EmitterLive(sanitizer: Sanitizer) extends Emitter:

  val RootType: String  = "root0"
  val IndentSpaces: Int = 2

  override def emit(node: Node): UIO[Option[String]] = emit0(node)

  def emit0(node: Node, ns: String = RootType, indent: Int = 0): UIO[Option[String]] =
    node match
      case NObject(fields, _) =>
        import ZIO.foreach as zfe
        for
          sNs      <- sanitizer.sanitize(ns)
          fields0  <- zfe(fields.map { (name, value) => emitField(value, name, sNs) })(identity)
          children <- zfe(fields.map { (name, value) => emit0(value, name, indent + 1) })(identity).map(_.flatten)
        yield
          val ind       = Seq.fill(indent * IndentSpaces)(" ").mkString
          val caseClass = s"${ind}case class $sNs(${fields0.mkString(", ")})"
          val companion = if (children.isEmpty) "" else children.mkString(s"\n${ind}object $sNs {\n", "\n", s"\n$ind}")

          (caseClass + companion).some
      case NArray(itemType, _) =>
        for
          sNs     <- sanitizer.sanitize(ns)
          emitted <- emit0(itemType, ns, indent)
        yield emitted
      case _ => ZIO.succeed(none[String])

  def emitField(node: Node, name: String, ns: String): UIO[String] =
    for
      sName <- sanitizer.sanitize(name)
      tpe   <- emitType(node, sName, ns.some)
    yield s"$sName: $tpe"

  def emitType(node: Node, name: String, ns: Option[String]): UIO[String] =
    def req(name: String, required: Boolean): String = if (required) name else s"Option[$name]"

    def emitArray(itemType: Node, required: Boolean) =
      for emitted <- emitType(itemType, name, ns)
      yield req(s"Seq[$emitted]", required)

    node match
      case NNull                      => ZIO.succeed("Option[String]")
      case NBoolean(required)         => ZIO.succeed(req("Boolean", required))
      case NNumber(required)          => ZIO.succeed(req("BigDecimal", required))
      case NString(required)          => ZIO.succeed(req("String", required))
      case NArray(itemType, required) => emitArray(itemType, required)
      case NObject(_, required)       => ZIO.succeed(req(s"${ns.fold("")(_ + ".")}$name", required))
