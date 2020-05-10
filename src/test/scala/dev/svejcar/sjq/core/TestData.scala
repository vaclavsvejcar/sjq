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

import dev.svejcar.sjq.core.Node.{ArrayN, NumberN, ObjectN, StringN}

import scala.collection.immutable.SeqMap

trait TestData {

  val RawJson1: String =
    s"""
       |{
       |  "first": "value11",
       |  "second": {
       |    "second1": "value21",
       |    "second2": 42
       |  },
       |  "third_t": [
       |    {
       |      "one": "value31",
       |      "two": 42
       |    },
       |    {
       |      "two": 42,
       |      "three": "foo"
       |    }
       |  ],
       |  "fourth": [],
       |  "fifth": ["hello", "world"]
       |}
     """.stripMargin

  val ParsedNode1: Node = ObjectN(
    fields = SeqMap(
      "first"  -> StringN(),
      "second" -> ObjectN(fields = SeqMap("second1" -> StringN(), "second2" -> NumberN())),
      "third_t" -> ArrayN(
        itemType = ObjectN(
          fields = SeqMap("one" -> StringN(required = false), "two" -> NumberN(), "three" -> StringN(required = false))
        )
      ),
      "fourth" -> ArrayN.empty,
      "fifth"  -> ArrayN(itemType = StringN())
    )
  )

  val EmittedNode1: String =
    """|case class root0(first: String, second: root0.second, `third_t`: Seq[root0.`third_t`], fourth: Seq[Option[String]], fifth: Seq[String])
       |object root0 {
       |  case class second(second1: String, second2: BigDecimal)
       |  case class `third_t`(one: Option[String], two: BigDecimal, three: Option[String])
       |}""".stripMargin

}
