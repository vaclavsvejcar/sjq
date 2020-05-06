package dev.svejcar.sjq.core

import cats.implicits._
import cats.kernel.Semigroup
import dev.svejcar.sjq.core.Node._
import io.circe.Json

object Parser {

  def parseJson(json: Json): Node = json.fold(
    jsonNull = Node.NullN,
    jsonBoolean = _ => BooleanN(),
    jsonNumber = _ => NumberN(),
    jsonString = _ => StringN(),
    jsonArray = arr =>
      arr.toList match {
        case Nil      => ArrayN.empty
        case x :: Nil => ArrayN(parseJson(x))
        case many     => ArrayN(many.map(parseJson).combineAllOption.getOrElse(ArrayN.empty))
      },
    jsonObject = obj => Node.ObjectN(obj.toMap.view.mapValues(parseJson).toMap)
  )

  implicit val semigroupNode: Semigroup[Node] = (x0: Node, y0: Node) =>
    (x0, y0) match {
      case (NullN, NullN)             => NullN
      case (NullN, _)                 => NullN
      case (_, NullN)                 => NullN
      case (x: BooleanN, y: BooleanN) => BooleanN(required = required(x, y))
      case (x: NumberN, y: NumberN)   => NumberN(required = required(x, y))
      case (x: StringN, y: StringN)   => StringN(required = required(x, y))
      case (x: ArrayN, y: ArrayN)     => ArrayN(itemType = x.itemType |+| y.itemType, required = required(x, y))
      case (x: ObjectN, y: ObjectN)   => ObjectN(fields = combineFields(x.fields, y.fields), required = required(x, y))
      case (x, y)                     => throw new IllegalArgumentException(s"Cannot combine different nodes '$x' with '$y'")
    }

  private def required(x: Node, y: Node): Boolean = x.required && y.required

  private def combineFields(fieldsX: Map[String, Node], fieldsY: Map[String, Node]): Map[String, Node] = {
    (fieldsX.toList ++ fieldsY.toList).groupBy(_._1).flatMap {
      case (name, Nil)              => none[(String, Node)]
      case (name, (_, node) :: Nil) => (name, notRequired(node)).some
      case (name, nodes)            => nodes.map(_._2).combineAllOption.map((name, _))
    }
  }

  private def notRequired(node: Node): Node = node match {
    case NullN       => NullN
    case x: BooleanN => x.copy(required = !x.required)
    case x: NumberN  => x.copy(required = !x.required)
    case x: StringN  => x.copy(required = !x.required)
    case x: ArrayN   => x.copy(required = !x.required)
    case x: ObjectN  => x.copy(required = !x.required)

  }

}
