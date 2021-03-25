package model

import io.circe._, io.circe.generic.auto._, io.circe.parser._


case class Swagger(definitions:Map[String,JsonSchemaItem])

object Swagger{

  implicit val decodeFoo: Decoder[Either[String,Seq[String]]] = new Decoder[Either[String,Seq[String]]] {
    final def apply(c: HCursor): Decoder.Result[Either[String,Seq[String]]] =

      c.as[String].toOption.map{ str => Left(str)}.orElse{ c.as[Seq[String]].toOption.map(seq => Right(seq))} match {
        case Some(s) => Right(s)
        case None => Left(DecodingFailure("Either error",List()))
      }
  }

  def parse(str:String):Option[Swagger] = {
    decode[Swagger](str).fold({ err =>
      println(err)
      None
    },Some(_))
  }
}



case class JsonSchemaItem(`type`:Option[Either[String,Seq[String]]],properties: Option[Map[String,JsonSchemaItem]],items: Option[JsonSchemaItem],`$ref`:Option[String]) {
  def ref = `$ref`.flatMap(_.split("/").lastOption)
  def typ:Option[String] = `type`.map {
    _ match {
      case Left(s) => s
      case Right(s) => s.headOption.getOrElse(JsonSchemaItem.Types.OBJECT)
    }
  }
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

