package model

import io.circe._, io.circe.generic.auto._, io.circe.parser._


case class Swagger(definitions:Map[String,JsonSchemaItem])

object Swagger{

//  implicit val decodeFoo: Decoder[Either[String,Seq[String]]] = new Decoder[Either[String,Seq[String]]] {
//    final def apply(c: HCursor): Decoder.Result[Either[String,Seq[String]]] =
//
//      c.as[String] match {
//        case Left(value) => {
//          println(s"Unable to decodeString for $c $value")
//          c.as[Seq[String]] match {
//            case Left(value) => Left(value)
//            case Right(value) => Right(Right(value))
//          }
//        }
//        case Right(value) => Right(Left(value))
//      }
//
//
//  }

  implicit def h[A,B](implicit a: Decoder[String], b: Decoder[Seq[String]]): Decoder[Either[String,Seq[String]]] = {
    val l: Decoder[Either[String,Seq[String]]]= a.map(Left.apply)
    val r: Decoder[Either[String,Seq[String]]]= b.map(Right.apply)
    l or r
  }

  def parse(str:String):Option[Swagger] = {
    io.circe.parser.parse(str) match {
      case Left(value) => {
        println(value)
        None
      }
      case Right(value) => value.deepDropNullValues.as[Swagger] match {
        case Left(value) => {
          println(value)
          None
        }
        case Right(value) => Some(value)
      }
    }

  }
}



case class JsonSchemaItem(`type`:Option[Either[String,Seq[String]]],properties: Option[Map[String,JsonSchemaItem]],items: Option[JsonSchemaItem],`$ref`:Option[String]) {
  def ref = `$ref`.flatMap(_.split("/").lastOption)
  def typ:String = `type`.flatMap {
    _ match {
      case Left(s) => Some(s)
      case Right(s) => s.headOption
    }
  }.getOrElse(JsonSchemaItem.Types.OBJECT)
}

object JsonSchemaItem{
  def parse(str:String):Option[JsonSchemaItem] = {
    decode[JsonSchemaItem](str).toOption
  }

  object Types {
    val OBJECT = "object"
    val ARRAY = "array"
    val STRING = "string"
    val INTEGER = "integer"
    val NUMBER = "number"
    val BOOLEAN = "boolean"
  }
}

