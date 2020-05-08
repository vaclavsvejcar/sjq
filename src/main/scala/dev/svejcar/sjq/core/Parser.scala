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
import cats.kernel.Semigroup
import dev.svejcar.sjq.core.Node._
import io.circe.Json

object Parser {

  def parseJson(json: Json): Node = json.fold(
    jsonNull = Node.NullN,
    jsonBoolean = _ => BooleanN(),
    jsonNumber = _ => NumberN(),
    jsonString = _ => StringN(),
    jsonArray = arr =>
      arr.toList match {
        case Nil      => ArrayN.empty
        case x :: Nil => ArrayN(parseJson(x))
        case many     => ArrayN(many.map(parseJson).combineAllOption.getOrElse(ArrayN.empty))
      },
    jsonObject = obj => Node.ObjectN(obj.toMap.view.mapValues(parseJson).toMap)
  )

  implicit val semigroupNode: Semigroup[Node] = (x0: Node, y0: Node) =>
    (x0, y0) match {
      case (NullN, NullN)             => NullN
      case (NullN, _)                 => NullN
      case (_, NullN)                 => NullN
      case (x: BooleanN, y: BooleanN) => BooleanN(required = required(x, y))
      case (x: NumberN, y: NumberN)   => NumberN(required = required(x, y))
      case (x: StringN, y: StringN)   => StringN(required = required(x, y))
      case (x: ArrayN, y: ArrayN)     => ArrayN(itemType = x.itemType |+| y.itemType, required = required(x, y))
      case (x: ObjectN, y: ObjectN)   => ObjectN(fields = combineFields(x.fields, y.fields), required = required(x, y))
      case (x, y)                     => throw new IllegalArgumentException(s"Cannot combine different nodes '$x' with '$y'")
    }

  private def required(x: Node, y: Node): Boolean = x.required && y.required

  private def combineFields(fieldsX: Map[String, Node], fieldsY: Map[String, Node]): Map[String, Node] = {
    (fieldsX.toList ++ fieldsY.toList).groupBy(_._1).flatMap {
      case (name, Nil)              => none[(String, Node)]
      case (name, (_, node) :: Nil) => (name, notRequired(node)).some
      case (name, nodes)            => nodes.map(_._2).combineAllOption.map((name, _))
    }
  }

  private def notRequired(node: Node): Node = node match {
    case NullN       => NullN
    case x: BooleanN => x.copy(required = !x.required)
    case x: NumberN  => x.copy(required = !x.required)
    case x: StringN  => x.copy(required = !x.required)
    case x: ArrayN   => x.copy(required = !x.required)
    case x: ObjectN  => x.copy(required = !x.required)

  }

}
