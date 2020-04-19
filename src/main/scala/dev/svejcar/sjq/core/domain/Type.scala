package dev.svejcar.sjq.core.domain

sealed trait Type {
  def name: String
  def priority: Int
}

sealed abstract class PlainType(val name: String, val priority: Int) extends Type with Product {
  def monad: Option[MonadType]
  def parent: Option[PlainType]
  def withMonad(monad: MonadType): PlainType
}

object PlainType {
  // dirty hack for Any - represented as Option[String], because Circe for some reason cannot derive with it
  case class Any(monad: Option[MonadType] = None, parent: Option[PlainType] = None)
      extends PlainType("Option[String]", 1) {
    override def withMonad(monad: MonadType): PlainType = this.copy(monad = Some(monad))
  }

  case class Boolean(monad: Option[MonadType] = None, parent: Option[PlainType] = None)
      extends PlainType("Boolean", 0) {
    override def withMonad(monad: MonadType): PlainType = this.copy(monad = Some(monad))
  }

  case class Number(monad: Option[MonadType] = None, parent: Option[PlainType] = None) extends PlainType("Double", 0) {
    override def withMonad(monad: MonadType): PlainType = this.copy(monad = Some(monad))
  }

  case class String(monad: Option[MonadType] = None, parent: Option[PlainType] = None) extends PlainType("String", 0) {
    override def withMonad(monad: MonadType): PlainType = this.copy(monad = Some(monad))
  }

  case class Custom(override val name: Predef.String, monad: Option[MonadType] = None, parent: Option[PlainType] = None)
      extends PlainType(name, 0) {
    override def withMonad(monad: MonadType): PlainType = this.copy(monad = Some(monad))
  }

}

sealed abstract class MonadType(val name: String, val priority: Int) extends Type
object MonadType {
  case object Option extends MonadType("Option", 1)
  case object Seq    extends MonadType("Seq", 0)
}
