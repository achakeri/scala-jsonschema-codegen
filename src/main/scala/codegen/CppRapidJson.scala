package codegen
import model.JsonSchemaItem

case class CppRapidJson(mainTitle:String,debug:Boolean) {


  def toFieldName(str:String) = str
  def toClassName(str:String) = if(str == "root") mainTitle.capitalize else str.capitalize

  object Classes extends CodeGen {
    override def generate(title: String, jsonSchema: JsonSchemaItem): String = s"""class ${toClassName(title)};"""
  }

  object Header extends CodeGen {
    override def generate(title: String, jsonSchema: JsonSchemaItem): String = {

      println(title)
      println(jsonSchema.properties)
      val props= jsonSchema.properties.get

      val fieldsCode = props.map{case (t,js) =>Fields.generate(t,js)}
      val getters = props.map{case (t,js) =>Getter.generate(t,js)}
      val setters = props.map{case (t,js) =>Setter.generate(t,js)}
      val checkers = props.map{case (t,js) =>Checker.generate(t,js)}



      s"""
         |class ${toClassName(title)} {
         |    public:
         |    	 ${toClassName(title)}(const Value& json);
         |         ${toClassName(title)}(const ${toClassName(title)}& obj);
         |         ${toClassName(title)}();
         |    	 ~${toClassName(title)}();
         |
         |    private:
         |        ${fieldsCode.mkString("\n        ")}
         |    public:
         |      void Serialize(Writer<StringBuffer>& writer) const;
         |      ${toClassName(title)}& operator=(const ${toClassName(title)}& obj);
         |      std::string toJsonString();
         |      ${getters.mkString("\n      ")}
         |      ${setters.mkString("\n      ")}
         |      ${checkers.mkString("\n      ")}
        |};
      """.stripMargin
    }
  }

  object Implementation extends CodeGen {
    override def generate(title: String, jsonSchema: JsonSchemaItem): String = {

      val props= jsonSchema.properties.get

      val fieldsDestructorCode = props.map{case (t,js) =>FieldsDestructor.generate(t,js)}
      val fieldsAssign = props.map{case (t,js) =>FieldsAssign.generate(t,js)}
      val fieldsCopy = props.map{case (t,js) =>FieldsCopy.generate(t,js)}
      val constructorCode = props.map{case (t,js) =>Constructor.generate(t,js)}
      val writerCode = props.map{case (t,js) =>Writer().generate(t,js)}


      s"""
         |
         |${toClassName(title)}::~${toClassName(title)}() {
         |         ${fieldsDestructorCode.mkString("\n          ")}
         |};
         |
         |${toClassName(title)}::${toClassName(title)}(const ${toClassName(title)}& obj) {
         |    ${fieldsCopy.mkString("\n          ")}
         |}
         |
         |${toClassName(title)}::${toClassName(title)}() { }
         |
         |${toClassName(title)}::${toClassName(title)}(const Value& json) {
         |           ${constructorCode.flatMap(_.split("\\\n")).mkString("\n            ")}
         |};
         |
         |${toClassName(title)}& ${toClassName(title)}::operator=(const ${toClassName(title)}& obj) {
         |    if (this != &obj)
         |        {
         |            ${fieldsAssign.mkString("\n          ")}
         |        }
         |        return *this;
         |}
         |
         |void ${toClassName(title)}::Serialize(Writer<StringBuffer>& writer) const {
         |        ${if(debug) s"""printf("Serializing ${toClassName(title)}\\n");""" else "" }
         |        writer.StartObject();
         |        ${writerCode.flatMap(_.split("\\\n")).mkString("\n        ")}
         |        writer.EndObject();
         |}
         |
         |std::string ${toClassName(title)}::toJsonString() {
         |    StringBuffer sb;
         |    Writer<StringBuffer> writer(sb);
         |		this->Serialize(writer);
         |		return std::string(sb.GetString());
         |}
         |
      """.stripMargin
    }
  }

  trait PointerValueCodeGen extends GenericCodeGen {
    def pointer(title: String, typ: String): String

    def value(title: String, typ: String): String

    override def obj(title: String, ref: String): String = pointer(title,Types.obj(title,ref))

    override def arr(title: String, items: JsonSchemaItem): String = pointer(title,Types.arr(title,items))

    override def bool(title: String): String = value(title,Types.bool(title))

    override def int(title: String): String = value(title,Types.int(title))

    override def num(title: String): String = value(title,Types.num(title))

    override def str(title: String): String = pointer(title,Types.str(title))
  }

  object Getter extends PointerValueCodeGen {
    def pointer(title: String, typ: String): String =
      s"""$typ* get_${toFieldName(title)}() { return ${toFieldName(title)}; }"""

    def value(title: String, typ: String): String =
      s"""$typ get_${toFieldName(title)}() { return *${toFieldName(title)}; }"""

  }

  object Setter extends PointerValueCodeGen {
    def pointer(title: String, typ:String): String =
      s"""void set_${toFieldName(title)}($typ* ${toFieldName(title)}) { this->${toFieldName(title)} = ${toFieldName(title)}; }"""

    def value(title: String, typ:String): String =
      s"""void set_${toFieldName(title)}($typ ${toFieldName(title)}) { this->${toFieldName(title)} = new $typ(${toFieldName(title)}); }"""

  }

  object Checker extends CodeGen {

    override def generate(title: String, jsonSchema: JsonSchemaItem): String =
      s"""bool has_${toFieldName(title)}() { return this->${toFieldName(title)} != nullptr; }"""


  }


  object Constructor extends GenericCodeGen {

    private def gen(title:String,f: GenericCodeGen => String):String = {
      s"""
         |if(json.HasMember("$title") && json["$title"].${f(CheckType)}) {
         |  ${toFieldName(title)} = new ${f(InitType())};
         |}
      """.stripMargin
    }

    override def obj(title: String, ref:String): String = gen(title,_.obj(title,ref))

    override def arr(title: String, items: JsonSchemaItem): String = {
      s"""
         |if(json.HasMember("$title") && json["$title"].IsArray()) {
         |			${toFieldName(title)} = new ${InitType().arr(title,items)};
         |			for (auto& m : json["$title"].GetArray()) {
         |				if(m.${CheckType.generate(title,items)}) {
         |					${toFieldName(title)}->push_back(${InitType(false).generate("m",items)});
         |				}
         |			}
         |		}
       """.stripMargin
    }

    override def bool(title: String): String = gen(title,_.bool(title))

    override def int(title: String): String = gen(title,_.int(title))

    override def num(title: String): String = gen(title,_.num(title))

    override def str(title: String): String = gen(title,_.str(title))
  }

  object FieldsDestructor extends GenericCodeGen {

    private def gen(title:String,f: GenericCodeGen => String):String = {
      s"""
         |if(${toFieldName(title)} != nullptr) delete ${toFieldName(title)};
         |""".stripMargin
    }

    override def obj(title: String, ref:String): String = gen(title,_.obj(title,ref))

    override def arr(title: String, items: JsonSchemaItem): String = gen(title,_.arr(title,items))

    override def bool(title: String): String = gen(title,_.bool(title))

    override def int(title: String): String = gen(title,_.int(title))

    override def num(title: String): String = gen(title,_.num(title))

    override def str(title: String): String = gen(title,_.str(title))
  }

  object FieldsAssign extends GenericCodeGen {

    private def gen(title:String,f: GenericCodeGen => String):String = {
      s"""if(obj.${toFieldName(title)} != nullptr) *${toFieldName(title)} = *obj.${toFieldName(title)};""".stripMargin
    }

    override def obj(title: String, ref:String): String = gen(title,_.obj(title,ref))

    override def arr(title: String, items: JsonSchemaItem): String = gen(title,_.arr(title,items))

    override def bool(title: String): String = gen(title,_.bool(title))

    override def int(title: String): String = gen(title,_.int(title))

    override def num(title: String): String = gen(title,_.num(title))

    override def str(title: String): String = gen(title,_.str(title))
  }

  object FieldsCopy extends GenericCodeGen {

    private def gen(title:String,f: GenericCodeGen => String):String = {
      s"""if(obj.${toFieldName(title)} != nullptr) ${toFieldName(title)} = new ${f(Types)}(*obj.${toFieldName(title)});""".stripMargin
    }

    override def obj(title: String, ref:String): String = gen(title,_.obj(title,ref))

    override def arr(title: String, items: JsonSchemaItem): String = gen(title,_.arr(title,items))

    override def bool(title: String): String = gen(title,_.bool(title))

    override def int(title: String): String = gen(title,_.int(title))

    override def num(title: String): String = gen(title,_.num(title))

    override def str(title: String): String = gen(title,_.str(title))
  }

  object Fields extends GenericCodeGen {

    private def gen(title:String,f: GenericCodeGen => String):String = {
      s"""${f(Types)}* ${toFieldName(title)} = nullptr;"""
    }

    override def obj(title: String, ref:String): String = gen(title,_.obj(title,ref))

    override def arr(title: String, items: JsonSchemaItem): String = gen(title,_.arr(title,items))

    override def bool(title: String): String = gen(title,_.bool(title))

    override def int(title: String): String = gen(title,_.int(title))

    override def num(title: String): String = gen(title,_.num(title))

    override def str(title: String): String = gen(title,_.str(title))
  }

  case class Writer(withHeader:Boolean = true) extends GenericCodeGen {

    private def gen(title:String)(writer:String):String = if(!withHeader) "" else {
      val debugMessage = if(debug) s"""printf("--> ${toFieldName(title)}\\n");""" else ""
      val main = s"""
         |if(${toFieldName(title)} != nullptr) {
         |  writer.String("$title");
         |  $writer
         |}
       """.stripMargin
       debugMessage ++ main
    }

    override def obj(title: String, ref:String): String = gen(title)(WriteType.obj(title,ref))

    override def arr(title: String, items: JsonSchemaItem): String = gen(title) {
      s"""
         |writer.StartArray();
         |for(std::size_t i=0; i< ${toFieldName(title)}->size(); ++i)  {
         |  ${Types.generate("obj",items)}* obj = &${toFieldName(title)}->at(i);
         |	${WriteType.generate("obj",items)}
         |}
         |writer.EndArray();
       """.stripMargin
    }

    override def bool(title: String): String = gen(title)(WriteType.bool(title))

    override def int(title: String): String = gen(title)(WriteType.int(title))

    override def num(title: String): String = gen(title)(WriteType.num(title))

    override def str(title: String): String = gen(title)(WriteType.str(title))
  }

  object Types extends GenericCodeGen {
    override def obj(title: String, ref:String): String = toClassName(ref)

    override def arr(title: String, items: JsonSchemaItem): String = s"std::vector<${Types.generate("",items)}>"

    override def bool(title: String): String = "bool"

    override def int(title: String): String = "int"

    override def num(title: String): String = "double"

    override def str(title: String): String = "std::string"
  }

  object CheckType extends GenericCodeGen {
    override def obj(title: String, ref:String): String = "IsObject()"

    override def arr(title: String, items: JsonSchemaItem): String = "IsArray()"

    override def bool(title: String): String = "IsBool()"

    override def int(title: String): String = "IsInt()"

    override def num(title: String): String = "IsNumber()"

    override def str(title: String): String = "IsString()"
  }

  case class InitType(rootType: Boolean = true) extends GenericCodeGen {

    private def field(title:String):String = if(rootType) s"""json["$title"]""" else title

    override def obj(title: String, ref:String): String =
      s"""${Types.obj(title,ref)}(${field(title)})"""

    override def arr(title: String, items: JsonSchemaItem): String =
      s"""std::vector<${Types.generate("",items)}>()"""

    override def bool(title: String): String = s"""bool(${field(title)}.GetBool())"""

    override def int(title: String): String = s"""int(${field(title)}.GetInt())"""

    override def num(title: String): String = s"""double(${field(title)}.GetDouble())"""

    override def str(title: String): String = s"""std::string(${field(title)}.GetString(),${field(title)}.GetStringLength())"""
  }

  object WriteType extends GenericCodeGen {
    override def obj(title: String, ref:String): String = s"${toFieldName(title)}->Serialize(writer);"

    override def arr(title: String, items: JsonSchemaItem): String = Writer(false).arr(title,items)

    override def bool(title: String): String = s"writer.Bool(*${toFieldName(title)});"

    override def int(title: String): String = s"writer.Int(*${toFieldName(title)});"

    override def num(title: String): String = s"writer.Double(*${toFieldName(title)});"

    override def str(title: String): String = s"writer.String(${toFieldName(title)}->c_str(), static_cast<SizeType>(${toFieldName(title)}->length()));"
  }

}
