package dev.svejcar.sjq.core

import dev.svejcar.sjq.core.domain.{CaseClass, Field, MonadType, Type}
import simulacrum.typeclass

@typeclass trait Merge[T] {
  def merge(t1: T, t2: T): T
}

object Merge {

  implicit def typeMerge[T <: Type]: Merge[T] = (t1: T, t2: T) => {
    Seq(t1, t2).minBy(_.priority)
  }

  implicit val fieldMerge: Merge[Field] = (t1: Field, t2: Field) => {
    require(t1.name == t2.name, "Attempting to merge two different field definitions")

    Seq(t1, t2).minBy(_.`type`.priority)
  }

  implicit val caseClassMerge: Merge[CaseClass] = (t1: CaseClass, t2: CaseClass) => {
    require(t1.name == t2.name, "Attempting to merge two different case class definitions")

    def merge(cc1: CaseClass, cc2: CaseClass): CaseClass = {
      val commonFields = cc1.fields intersect cc2.fields

      def combineFields(cc: CaseClass): Set[Field] = (cc.fields diff commonFields).map { field =>
        val monadType =
          field.`type`.monad.map(Merge[MonadType].merge(_, MonadType.Option)).getOrElse(MonadType.Option)
        field.copy(`type` = field.`type`.withMonad(monadType))
      }

      val newFields = (commonFields ++ combineFields(cc1) ++ combineFields(cc2))
        .groupBy(_.name)
        .view
        .mapValues(_.reduceLeft((aggr, next) => Merge[Field].merge(aggr, next)))
        .values
        .toSet

      val allChildren = cc1.children ++ cc2.children
      val mergedNodes = allChildren.groupBy(_.name).values.filter(_.size > 1).map(_.reduceLeft(merge)).toSet

      val newNodes = mergedNodes ++ allChildren.filterNot(nodes => mergedNodes.map(_.name).contains(nodes.name))

      CaseClass(cc1.name, newFields, newNodes)
    }

    merge(t1, t2)
  }
}
