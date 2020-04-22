package dev.svejcar.sjq.core

import cats.implicits._
import dev.svejcar.sjq.core.instances.all._
import utest._

object CaseClassSuite extends TestSuite {
  import domain._

  override def tests: Tests = Tests {

    test("Two case class definitions of same name should be merged correctly") {
      val ci11 =
        CaseClass("field1", Set(Field("field1", PlainType.String()), Field("field2", PlainType.String())), Set.empty)
      val ci12 =
        CaseClass("field1", Set(Field("field1", PlainType.String()), Field("field3", PlainType.String())), Set.empty)
      val ci2 =
        CaseClass("field2", Set(Field("field1", PlainType.String()), Field("field3", PlainType.String())), Set.empty)
      val cc1 = CaseClass(
        "test",
        Set(Field("field1", PlainType.Custom("field1")), Field("field2", PlainType.String())),
        Set(ci11, ci2)
      )
      val cc2 = CaseClass(
        "test",
        Set(Field("field1", PlainType.Custom("field1")), Field("field3", PlainType.String())),
        Set(ci12, ci2)
      )

      val expected = CaseClass(
        "test",
        Set(
          Field("field1", PlainType.Custom("field1")),
          Field("field2", PlainType.String(monad = Some(MonadType.Option))),
          Field("field3", PlainType.String(monad = Some(MonadType.Option)))
        ),
        Set(ci2) + CaseClass(
          "field1",
          Set(
            Field("field1", PlainType.String()),
            Field("field2", PlainType.String(monad = Some(MonadType.Option))),
            Field("field3", PlainType.String(monad = Some(MonadType.Option)))
          ),
          Set.empty
        )
      )

      val actual = cc1 |+| cc2
      assert(actual == expected)
    }
  }
}
