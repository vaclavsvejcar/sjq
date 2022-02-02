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

import cats.implicits.*
import cats.kernel.Semigroup
import dev.svejcar.sjq.model.Node
import dev.svejcar.sjq.model.Node.*
import io.circe.{Json, JsonObject}
import zio.*

trait Parser:
  def parseJson(json: Json): UIO[Node]

object Parser:
  val live: ULayer[ParserLive] = ZLayer.succeed(ParserLive())

case class ParserLive() extends Parser:
  override def parseJson(json: Json): UIO[Node] =
    import ZIO.foreach as zfe
    json.fold(
      jsonNull = ZIO.succeed(NNull),
      jsonBoolean = _ => ZIO.succeed(NBoolean()),
      jsonNumber = _ => ZIO.succeed(NNumber()),
      jsonString = _ => ZIO.succeed(NString()),
      jsonArray = (arr: Vector[Json]) =>
        arr.toList match
          case Nil      => ZIO.succeed(NArray.empty)
          case x :: Nil => parseJson(x).map(NArray(_))
          case many     => ZIO.foreach(many)(parseJson).map(_.combineAllOption.getOrElse(NArray.empty)).map(NArray(_))
      ,
      jsonObject = obj =>
        for pairs <- ZIO.foreach(obj.toList)((key, value) => parseJson(value).map((key, _)))
        yield NObject(pairs.toMap)
    )

  given Semigroup[Node] with
    def combine(x0: Node, y0: Node): Node =
      (x0, y0) match {
        case (NNull, NNull)             => NNull
        case (NNull, _)                 => NNull
        case (_, NNull)                 => NNull
        case (x: NBoolean, y: NBoolean) => NBoolean(required = required(x, y))
        case (x: NNumber, y: NNumber)   => NNumber(required = required(x, y))
        case (x: NString, y: NString)   => NString(required = required(x, y))
        case (x: NArray, y: NArray)     => NArray(itemType = x.itemType |+| y.itemType, required = required(x, y))
        case (x: NObject, y: NObject) => NObject(fields = combineFields(x.fields, y.fields), required = required(x, y))
        case (x, y) => throw new IllegalArgumentException(s"Cannot combine different nodes '$x' with '$y'")
      }

  def combineFields(fieldsX: Map[String, Node], fieldsY: Map[String, Node]): Map[String, Node] =
    (fieldsX.toList ++ fieldsY.toList).groupBy(_._1).flatMap {
      case (name, Nil)              => none[(String, Node)]
      case (name, (_, node) :: Nil) => (name, unrequire(node)).some
      case (name, nodes)            => nodes.map(_._2).combineAllOption.map((name, _))
    }

  def required(x: Node, y: Node): Boolean = x.required && y.required

  def unrequire(node: Node): Node = node match
    case NNull       => NNull
    case x: NBoolean => x.copy(required = !x.required)
    case x: NNumber  => x.copy(required = !x.required)
    case x: NString  => x.copy(required = !x.required)
    case x: NArray   => x.copy(required = !x.required)
    case x: NObject  => x.copy(required = !x.required)
