package dev.svejcar.sjq.model

sealed trait Node:
  def required: Boolean

object Node:
  case object NNull                                                       extends Node { override val required = false }
  case class NBoolean(required: Boolean = true)                           extends Node
  case class NNumber(required: Boolean = true)                            extends Node
  case class NString(required: Boolean = true)                            extends Node
  case class NArray(itemType: Node, required: Boolean = true)             extends Node
  case class NObject(fields: Map[String, Node], required: Boolean = true) extends Node

  object NArray:
    val empty: NArray = NArray(NNull)
