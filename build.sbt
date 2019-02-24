ThisBuild / scalaVersion := "2.12.7"
ThisBuild / organization := "com.cfox70"

lazy val parser = (project in file("."))
  .settings(
    name := "parser1",
    libraryDependencies += "com.eed3si9n" %% "gigahorse-okhttp" % "0.3.1",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test,

  )

