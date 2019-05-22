import Dependencies._

ThisBuild / scalaVersion := "2.12.8"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

val circeVersion = "0.10.0"



lazy val root = (project in file("."))

  .settings(
    name := "tdr-containers",
    addCompilerPlugin(
      "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
    ),
    libraryDependencies += scalaTest % Test,
    libraryDependencies += "commons-io" % "commons-io" % "2.6",
    libraryDependencies += "fi.solita.clamav" % "clamav-client" % "1.0.0",
    libraryDependencies += "com.softwaremill.sttp" %% "core" % "1.5.17",
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk" % "1.11.80",
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)

  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
