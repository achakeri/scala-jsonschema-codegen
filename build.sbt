import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.example",
      scalaVersion := "2.13.6",
      version      := "0.2.0-SNAPSHOT"
    )),
    name := "scala-jsonschema-codegen",
    //libraryDependencies += scalaTest % Test,
    libraryDependencies ++= circe
  )
