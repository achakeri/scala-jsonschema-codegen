import java.io.{File, PrintWriter}

import codegen.CppRapidJson
import model.Swagger

import scala.io.Source

object Main extends App {
  val swagger = Source.fromFile(args(0)).getLines.mkString(" ")

  val cpp = CppRapidJson("configuration",false)


  Swagger.parse(swagger).map{sw =>
    val (headers,implementations,classes) = sw.definitions.map{ case (title,jsonSchema) =>
      (cpp.Header.generate(title,jsonSchema),cpp.Implementation.generate(title,jsonSchema),cpp.Classes.generate(title,jsonSchema))
    }.unzip3

    val topHeader =
      """
        |#pragma once
        |
        |#include <vector>
        |#include <string>
        |
        |#include "rapidjson/document.h"
        |#include "rapidjson/writer.h"
        |#include "rapidjson/stringbuffer.h"
        |
        |using namespace rapidjson;
        |
        |""".stripMargin

    var topImpl =
      """
        |#include "Models.h"
      """.stripMargin



    val writerHeader = new PrintWriter(new File("model.h"))
    writerHeader.write(topHeader + (classes ++ headers).mkString("\n"))
    writerHeader.close()

    val writerImlp = new PrintWriter(new File("model.cpp"))
    writerImlp.write(topImpl + implementations.mkString("\n"))
    writerImlp.close()

  }
}
