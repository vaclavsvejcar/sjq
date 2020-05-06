package dev.svejcar.sjq.core

sealed trait Node { def required: Boolean }
object Node {
  case object NullN                                                       extends Node { override val required = false }
  case class BooleanN(required: Boolean = true)                           extends Node
  case class NumberN(required: Boolean = true)                            extends Node
  case class StringN(required: Boolean = true)                            extends Node
  case class ArrayN(itemType: Node, required: Boolean = true)             extends Node
  case class ObjectN(fields: Map[String, Node], required: Boolean = true) extends Node

  object ArrayN { val empty: ArrayN = ArrayN(NullN) }
}
