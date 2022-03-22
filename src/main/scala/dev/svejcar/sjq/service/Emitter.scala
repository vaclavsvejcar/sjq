/*
 * sjq :: Command-line JSON processor
 * Copyright (c) 2020-2022 Vaclav Svejcar
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

package dev.svejcar.sjq.service

import cats.implicits._
import dev.svejcar.sjq.model.Node
import dev.svejcar.sjq.model.Node._
import zio._

trait Emitter {
  def emit(node: Node): UIO[Option[String]]
  def emitRoot(node: Node): UIO[String]
}

object Emitter {
  val live: URLayer[Sanitizer, Emitter] = (EmitterLive(_)).toLayer[Emitter]

  def emit(node: Node): ZIO[Emitter, Nothing, Option[String]] = ZIO.serviceWithZIO[Emitter](_.emit(node))
  def emitRoot(node: Node): ZIO[Emitter, Nothing, String] = ZIO.serviceWithZIO[Emitter](_.emitRoot(node))
}

case class EmitterLive(sanitizer: Sanitizer) extends Emitter {

  val RootType: String  = "root0"
  val IndentSpaces: Int = 2

  override def emit(node: Node): UIO[Option[String]] = emit0(node)

  override def emitRoot(node: Node): UIO[String] = emitType(node, RootType, None)

  def emit0(node: Node, rawNs: String = RootType, indent: Int = 0): UIO[Option[String]] =
    node match {
      case NObject(fields, _) =>
        for {
          ns       <- sanitizer.sanitize(rawNs)
          fields0  <- ZIO.foreach(fields.toList) { case (name, value) => emitField(value, name, ns) }
          children <- ZIO.foreach(fields.toList) { case (name, value) => emit0(value, name, indent + 1) }.map(_.flatten)
        } yield {
          val ind       = Seq.fill(indent * IndentSpaces)(" ").mkString
          val caseClass = s"${ind}case class $ns(${fields0.mkString(", ")})"
          val companion = if (children.isEmpty) "" else children.mkString(s"\n${ind}object $ns {\n", "\n", s"\n$ind}")

          (caseClass + companion).some
        }
      case NArray(itemType, _) => emit0(itemType, rawNs, indent)
      case _                   => ZIO.succeed(none[String])
    }

  def emitField(node: Node, rawName: String, ns: String): UIO[String] =
    for {
      name <- sanitizer.sanitize(rawName)
      tpe  <- emitType(node, name, ns.some)
    } yield s"$name: $tpe"

  def emitType(node: Node, name: String, ns: Option[String]): UIO[String] = {
    def req(name: String, required: Boolean): String = if (required) name else s"Option[$name]"

    def emitArray(itemType: Node, required: Boolean) =
      for {
        emitted <- emitType(itemType, name, ns)
      } yield req(s"Seq[$emitted]", required)

    node match {
      case NNull                      => ZIO.succeed("Option[String]")
      case NBoolean(required)         => ZIO.succeed(req("Boolean", required))
      case NNumber(required)          => ZIO.succeed(req("BigDecimal", required))
      case NString(required)          => ZIO.succeed(req("String", required))
      case NArray(itemType, required) => emitArray(itemType, required)
      case NObject(_, required)       => ZIO.succeed(req(s"${ns.fold("")(_ + ".")}$name", required))
    }
  }
}
