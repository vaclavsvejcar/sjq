package dev.svejcar.sjq.core.instances

import cats.Semigroup
import cats.implicits._
import dev.svejcar.sjq.core.domain.{CaseClass, Field, MonadType, Type}

trait SemigroupInstances {

  implicit def typeSemigroup[T <: Type]: Semigroup[T] = (x: T, y: T) => Seq(x, y).minBy(_.priority)

  implicit val fieldSemigroup: Semigroup[Field] = (x: Field, y: Field) => {
    require(x.name == y.name, "Attempting to merge two different field definitions")

    Seq(x, y).minBy(_.`type`.priority)
  }

  implicit val caseClassSemigroup: Semigroup[CaseClass] = (x: CaseClass, y: CaseClass) => {
    require(x.name == y.name, "Attempting to merge two different case class definitions")

    def merge(cc1: CaseClass, cc2: CaseClass): CaseClass = {
      val commonFields = cc1.fields intersect cc2.fields

      def combineFields(cc: CaseClass): Set[Field] = (cc.fields diff commonFields).map { field =>
        val monadType = field.`type`.monad.map(_ |+| MonadType.Option).getOrElse(MonadType.Option)
        field.copy(`type` = field.`type`.withMonad(monadType))
      }

      val newFields = (commonFields ++ combineFields(cc1) ++ combineFields(cc2))
        .groupBy(_.name)
        .view
        .mapValues(_.reduceLeft((aggr, next) => aggr |+| next))
        .values
        .toSet

      val allChildren = cc1.children ++ cc2.children
      val mergedNodes = allChildren.groupBy(_.name).values.filter(_.size > 1).map(_.reduceLeft(merge)).toSet

      val newNodes = mergedNodes ++ allChildren.filterNot(nodes => mergedNodes.map(_.name).contains(nodes.name))

      CaseClass(cc1.name, newFields, newNodes)
    }

    merge(x, y)
  }
}
