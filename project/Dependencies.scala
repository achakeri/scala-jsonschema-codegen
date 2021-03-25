import sbt._

object Dependencies {
  private lazy val circeVersion = "0.9.3"

  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"

  lazy val circe = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion)

}
