// in the name of ALLAH

organization := "com.bisphone"

name := "std"

version := "0.13.1"

scalaVersion := "2.11.12"

crossScalaVersions := Seq("2.11.12", "2.12.6")

scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-unchecked",
    "-language:postfixOps",
    "-language:implicitConversions"
)

fork := true

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.5" % Test


