package dev.svejcar.sjq.core

import io.circe.Json

object Parser {
  import domain._

  private val JsNull: Json = io.circe.parser.parse("null").toOption.get

  def parseDefinitions(json: Json): CaseClass = {
    def parse(name: String, value: Json, parent: CaseClass, monad: Option[MonadType]): CaseClass = {
      def parentWith(`type`: PlainType): CaseClass = parent.addField(Field(name, `type`))

      value.fold(
        jsonNull = parentWith(PlainType.Any(monad = monad)),
        jsonBoolean = _ => parentWith(PlainType.Boolean(monad = monad)),
        jsonNumber = _ => parentWith(PlainType.Number(monad = monad)),
        jsonString = _ => parentWith(PlainType.String(monad = monad)),
        jsonArray = arr => {
          if (arr.isEmpty) {
            parse(name, JsNull, parent, Some(MonadType.Seq))
          } else {
            arr.foldLeft(parent)((pParent, pJson) => parse(name, pJson, pParent, Some(MonadType.Seq)))
          }
        },
        jsonObject = obj =>
          parent
            .addField(Field(name, PlainType.Custom(name, monad = monad, parent = Some(PlainType.Custom(parent.name)))))
            .addChild(obj.toMap.foldLeft(CaseClass.empty(name)) {
              case (curr, (fName, fValue)) => parse(fName, fValue, curr, None)
            })
      )
    }

    parse("root", json, CaseClass.Root, None).children.head
  }

}
