package dev.svejcar.sjq

package object core {
  import domain._

  val RawJson: String =
    s"""
       |{
       |  "first": "value11",
       |  "second": {
       |    "second1": "value21",
       |    "second2": 42
       |  },
       |  "third": [
       |    {
       |      "third1": "value31",
       |      "third2": 42
       |    }
       |  ],
       |  "fourth": []
       |}
     """.stripMargin

  val ParsedDefinitions: CaseClass = {
    val second =
      CaseClass("second", Set(Field("second1", PlainType.String()), Field("second2", PlainType.Number())), Set.empty)
    val third =
      CaseClass("third", Set(Field("third1", PlainType.String()), Field("third2", PlainType.Number())), Set.empty)

    CaseClass(
      "root",
      Set(
        Field("first", PlainType.String()),
        Field("second", PlainType.Custom("second", parent = Some(PlainType.Custom("root")))),
        Field("third", PlainType.Custom("third", monad = Some(MonadType.Seq), parent = Some(PlainType.Custom("root")))),
        Field("fourth", PlainType.Any(monad = Some(MonadType.Seq)))
      ),
      Set(second, third)
    )
  }

  val RenderedCode: String =
    s"""
       |case class root(first: String, second: root.second, third: Seq[root.third], fourth: Seq[Option[String]])
       |object root {
       |case class second(second1: String, second2: Double)
       |case class third(third1: String, third2: Double)
       |}
     """.stripMargin.trim
}
