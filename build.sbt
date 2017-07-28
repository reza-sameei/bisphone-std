// in the name of ALLAH

val jvmTarget = SettingKey[String]("jvmTarget")

jvmTarget in ThisBuild := "1.8"

val globe = Seq(
    organization := "com.bisphone",
    scalaVersion := "2.11.11",
    crossScalaVersions := Seq("2.11.11", "2.12.3"),
    fork := true,
    scalacOptions ++= Seq(
        "-feature",
        "-deprecation",
        "-language:postfixOps",
        "-language:implicitConversions",
        s"-target:jvm-${jvmTarget.value}",
        // https://tpolecat.github.io/2014/04/11/scalac-flags.html
        "-encoding", "UTF-8",
        // "-Xfatal-warnings",
        "-Xlint",
        "-Yno-adapted-args",
        "-Ywarn-dead-code",        // N.B. doesn't work well with the ??? hole
        "-Ywarn-numeric-widen",
        "-Ywarn-value-discard",
        "-Xfuture",
        // "-Ywarn-unused-import",
        "-Ylog-classpath"
    ),
    javacOptions ++= Seq("-source", jvmTarget.value, "-target", jvmTarget.value)
)

lazy val root = (project in file("."))
    .settings(globe: _*)
    .settings(
            name := "std"
        ,   version := "1.0.0-SNAPSHOT"
        ,   libraryDependencies ++= Seq(
            "com.typesafe" % "config" % "1.3.0"
            // ,   "org.slf4j" % "slf4j-api" % "1.7.25"
            ,   "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
            ,   "org.scalatest" %% "scalatest" % "3.0.1" % Test
        )
        // ,   libraryDependencies += "com.bisphone" %% "beta-testkit" % "0.1.0" % Test
    )


