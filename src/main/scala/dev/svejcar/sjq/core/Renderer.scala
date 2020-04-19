package dev.svejcar.sjq.core

import simulacrum._

@typeclass trait Renderer[T] { def renderCode(obj: T): String }

object Renderer {
  import Renderer.ops._
  import Sanitizer.sanitize
  import domain._

  implicit val typeRenderer: Renderer[PlainType] = (obj: PlainType) => {
    val name = obj.parent.fold(sanitize(obj.name))(parent => s"${sanitize(parent.name)}.${sanitize(obj.name)}")
    obj.monad.fold(name)(monad => s"${monad.name}[$name]")
  }

  implicit val fieldRenderer: Renderer[Field] = (obj: Field) => {
    val name = sanitize(obj.name)
    val tpe  = obj.`type`.renderCode

    s"$name: $tpe"
  }

  implicit val caseClassRenderer: Renderer[CaseClass] = (obj: CaseClass) => {
    def impl(cc: CaseClass): String = {
      val name   = sanitize(cc.name)
      val fields = cc.fields.map(_.renderCode).mkString(", ")
      val nodes  = cc.children.map(impl).mkString("\n")

      def objectRenderer: String =
        if (cc.children.isEmpty) ""
        else
          s"""
             |object $name {
             |$nodes
             |}
       """.stripMargin.trim

      s"""
         |case class $name($fields)
         |$objectRenderer
       """.stripMargin.trim
    }

    impl(obj)
  }
}
