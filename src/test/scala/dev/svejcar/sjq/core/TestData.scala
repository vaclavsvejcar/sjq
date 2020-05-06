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
       |  case class second(second1: String, second2: Double)
       |  case class `third_t`(one: Option[String], two: Double, three: Option[String])
       |}""".stripMargin

}
