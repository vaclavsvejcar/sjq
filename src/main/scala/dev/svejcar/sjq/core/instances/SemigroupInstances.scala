package dev.svejcar.sjq.core.instances

import cats.Semigroup
import cats.implicits._
import dev.svejcar.sjq.core.domain.{CaseClass, Field, MonadType, Type}

trait SemigroupInstances {

  implicit def typeSemigroup[T <: Type]: Semigroup[T] = (x: T, y: T) => Seq(x, y).minBy(_.priority)

  implicit val fieldSemigroup: Semigroup[Field] = (x: Field, y: Field) => {
    require(x.name == y.name, "Attempting to combine two different field definitions")
    x.copy(`type` = x.`type` |+| y.`type`)
  }

  implicit val caseClassSemigroup: Semigroup[CaseClass] = (x: CaseClass, y: CaseClass) => {
    require(x.name == y.name, "Attempting to combine two different case classes")

    val newFields: Set[Field] = (x.fields.toSeq ++ y.fields.toSeq)
      .groupBy(_.name)
      .flatMap {
        case (_, field :: Nil)            => field.copy(`type` = field.`type`.withMonad(MonadType.Option)).some
        case (_, field1 :: field2 :: Nil) => (field1 |+| field2).some
        case _                            => none[Field]
      }
      .toSet

    val newChildren: Set[CaseClass] = (x.children.toSeq ++ y.children)
      .groupBy(_.name)
      .flatMap {
        case (_, child :: Nil)            => child.some
        case (_, child1 :: child2 :: Nil) => (child1 |+| child2).some
        case _                            => none[CaseClass]

      }
      .toSet

    x.copy(fields = newFields, children = newChildren)
  }
}
