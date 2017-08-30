// in the name of ALLAH

organization := "com.bisphone"

name := "std"

version := "0.11.0"

scalaVersion := "2.11.11"

crossScalaVersions := Seq("2.11.11", "2.12.3")

scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-unchecked",
    "-language:postfixOps",
    "-language:implicitConversions"
)

fork := true

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % Test


