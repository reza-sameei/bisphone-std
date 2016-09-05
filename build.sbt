// in the name of ALLAH

organization := "com.bisphone"

name := "std"

version := "0.7.0-SNAPSHOT"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-feature", "-deprecation", "-language:postfixOps")

fork := true

def akka(artifact: String, version:String = "2.4.4") = "com.typesafe.akka" %% artifact % version

def testkit = Seq("org.scalatest" %% "scalatest" % "2.2.6" % Test)

libraryDependencies ++= Seq(
  // "com.typesafe" % "config" % "1.3.0"
)


