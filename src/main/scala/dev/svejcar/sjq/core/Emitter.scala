package dev.svejcar.sjq.core

import cats.implicits._
import dev.svejcar.sjq.core.Node._
import dev.svejcar.sjq.core.Sanitizer.sanitize

object Emitter {

  val RootType: String  = "root0"
  val IndentSpaces: Int = 2

  def emitType(node: Node, name: String, ns: Option[String]): String = node match {
    case NullN                      => "Option[String]"
    case BooleanN(required)         => req("Boolean", required)
    case NumberN(required)          => req("Double", required)
    case StringN(required)          => req("String", required)
    case ArrayN(itemType, required) => req(s"Seq[${emitType(itemType, name, ns)}]", required)
    case ObjectN(_, required)       => req(s"${ns.fold("")(_ + ".")}$name", required)
  }

  def emitField(node: Node, name: String, ns: String): String = s"$name: ${emitType(node, name, ns.some)}"

  def emit(node: Node, ns: String = RootType, indent: Int = 0): Option[String] = {
    val ind = Seq.fill(indent * IndentSpaces)(" ").mkString
    node match {
      case ObjectN(fields, _) =>
        val fields0  = fields.map { case (name, value) => emitField(value, sanitize(name), sanitize(ns)) }
        val children = fields.flatMap { case (name, value) => emit(value, sanitize(name), indent + 1) }.toList

        val caseClass = s"${ind}case class $ns(${fields0.mkString(", ")})"
        val companion = if (children.isEmpty) "" else children.mkString(s"\n${ind}object $ns {\n", "\n", s"\n$ind}")

        (caseClass + companion).some
      case ArrayN(itemType, _) =>
        emit(itemType, ns, indent)
      case _ => none[String]
    }
  }

  def req(name: String, required: Boolean): String = if (required) name else s"Option[$name]"

}
