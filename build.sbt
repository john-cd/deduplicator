name := "deduplicator"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

// https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12
libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.25" % Test


libraryDependencies += "com.typesafe" % "config" % "1.3.1"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.6",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.6" % Test
)

// https://mvnrepository.com/artifact/com.h2database/h2
libraryDependencies += "com.h2database" % "h2" % "1.4.196" //% Test
