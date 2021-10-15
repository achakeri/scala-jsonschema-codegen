package codegen
import model.JsonSchemaItem

trait GenericCodeGen extends CodeGen {
  override def generate(title:String,jsonSchema: JsonSchemaItem): String = {
    if(jsonSchema.ref.isDefined) obj(title,jsonSchema.ref.get) else {
      jsonSchema.typ match {
        case JsonSchemaItem.Types.ARRAY => arr(title,jsonSchema.items.get)
        case JsonSchemaItem.Types.BOOLEAN => bool(title)
        case JsonSchemaItem.Types.INTEGER => int(title)
        case JsonSchemaItem.Types.NUMBER => num(title)
        case JsonSchemaItem.Types.STRING => str(title)
        case s:String => throw new Exception(s"Didn't found handler for type: $s")
      }
    }
  }.trim

  def obj(title:String,ref:String):String
  def arr(title:String,items:JsonSchemaItem):String
  def bool(title:String):String
  def int(title:String):String
  def num(title:String):String
  def str(title:String):String
}
