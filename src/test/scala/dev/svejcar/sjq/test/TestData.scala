package dev.svejcar.sjq.test

import dev.svejcar.sjq.model.Node
import dev.svejcar.sjq.model.Node.{NArray, NNumber, NObject, NString}

import scala.collection.immutable.SeqMap

trait TestData:

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

  val ParsedNode1: Node = NObject(
    fields = SeqMap(
      "first"  -> NString(),
      "second" -> NObject(fields = SeqMap("second1" -> NString(), "second2" -> NNumber())),
      "third_t" -> NArray(
        itemType = NObject(
          fields = SeqMap("one" -> NString(required = false), "two" -> NNumber(), "three" -> NString(required = false))
        )
      ),
      "fourth" -> NArray.empty,
      "fifth"  -> NArray(itemType = NString())
    )
  )

  val EmittedNode1: String =
    """|case class root0(first: String, second: root0.second, `third_t`: Seq[root0.`third_t`], fourth: Seq[Option[String]], fifth: Seq[String])
       |object root0 {
       |  case class second(second1: String, second2: BigDecimal)
       |  case class `third_t`(one: Option[String], two: BigDecimal, three: Option[String])
       |}""".stripMargin
