/*
 * sjq :: Command-line JSON processor
 * Copyright (c) 2020 Vaclav Svejcar
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
    case NumberN(required)          => req("BigDecimal", required)
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
