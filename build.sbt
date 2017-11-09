// in the name of ALLAH

organization := "com.bisphone"

name := "std"

version := "0.12.0-SNAPSHOT"

scalaVersion := "2.11.11"

crossScalaVersions := Seq("2.11.11", "2.12.4")

scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-unchecked",
    "-language:postfixOps",
    "-language:implicitConversions"
)

fork := true

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % Test


