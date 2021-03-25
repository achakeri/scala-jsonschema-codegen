package codegen

import model.JsonSchemaItem


trait CodeGen {
  def generate(title:String,jsonSchema:JsonSchemaItem):String
}
