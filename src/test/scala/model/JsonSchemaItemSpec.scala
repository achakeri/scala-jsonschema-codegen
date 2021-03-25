//package model
//
//
//import org.scalatest._
//
//class JsonSchemaItemSpec extends FlatSpec with Matchers {
//  "The model" should "parse a simple schema" in {
//    val simpleSchema =
//      """
//        |{
//        |
//        |  "$schema": "http://json-schema.org/draft-06/schema#",
//        |  "description": "A geographical coordinate aaa",
//        |    "title": "GPO",
//        |    "type": "object",
//        |    "properties": {
//        |        "mode": {
//        |            "type": "string",
//        |            "enum": [
//        |                "Disabled",
//        |                "Continuous",
//        |                "Triggered"
//        |            ]
//        |        },
//        |        "policy": {
//        |            "type": "integer"
//        |        },
//        |        "codes_timeout": {
//        |            "type": "integer",
//        |            "default": "50",
//        |            "minimum": 0,
//        |            "maximum": 9999
//        |        },
//        |        "enable_combo_length_filter": {
//        |            "type": "boolean"
//        |        },
//        |        "combo_length_filter": {
//        |            "type": "integer",
//        |            "default": "12",
//        |            "minimum": 1,
//        |            "maximum": 164
//        |        }
//        |    },
//        |    "required": [
//        |        "mode",
//        |        "enable_combo_length_filter"
//        |    ]
//        |
//        |}
//      """.stripMargin
//
//      val schema = JsonSchemaItem.parse(simpleSchema)
//
//      schema.isDefined shouldBe true
//      schema.get.typ shouldBe "object"
//      val mode = schema.get.properties.flatMap(_.get("mode"))
//      mode.isDefined shouldBe true
//      mode.get.typ shouldBe "string"
//  }
//}
