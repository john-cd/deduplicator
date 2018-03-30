name := "deduplicator"

version := "0.1"

scalaVersion := "2.12.4"

// Logs
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"

// https://mvnrepository.com/artifact/org.slf4j/slf4j-log4j12
libraryDependencies += "org.slf4j" % "slf4j-log4j12" % "1.7.25" //% Test

// Command Line Interface
libraryDependencies += "com.github.scopt" %% "scopt" % "3.7.0"

// Config
libraryDependencies += "com.typesafe" % "config" % "1.3.1"

// Commons
libraryDependencies += "commons-codec" % "commons-codec" % "1.11"

// Migration
libraryDependencies += "org.flywaydb" % "flyway-core" % "4.2.0"

// Database		
// https://mvnrepository.com/artifact/com.h2database/h2
libraryDependencies += "com.h2database" % "h2" % "1.4.196" //% Test

// Tests
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

// Guava
// https://mvnrepository.com/artifact/com.google.guava/guava
libraryDependencies += "com.google.guava" % "guava" % "24.1-jre"
