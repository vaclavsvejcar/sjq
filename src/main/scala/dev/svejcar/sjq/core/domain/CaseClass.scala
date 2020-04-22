package dev.svejcar.sjq.core.domain

import cats.implicits._
import dev.svejcar.sjq.core.instances.all._

case class CaseClass(name: String, fields: Set[Field], children: Set[CaseClass]) {

  def addField(field: Field): CaseClass = {
    val newField = fields.find(_.name == field.name).fold(field)(_ |+| field)
    this.copy(fields = fields.filterNot(_.name == field.name) + newField)
  }

  def addChild(child: CaseClass): CaseClass = {
    val newChild = children.find(_.name == child.name).fold(child)(_ |+| child)
    this.copy(children = children.filterNot(_.name == child.name) + newChild)
  }
}

object CaseClass {
  val Root: CaseClass                = empty("root")
  def empty(name: String): CaseClass = CaseClass(name, Set.empty, Set.empty)
}
