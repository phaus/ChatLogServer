name := """ChatLogServer"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

maintainer in Docker := "Philipp Haußleiter <philipp.haussleiter@innoq.com>"

packageName in Docker := packageName.value

version in Docker := version.value

dockerExposedPorts in Docker := Seq(9000)

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "mysql" % "mysql-connector-java" % "5.1.18",
  "com.google.guava" % "guava" % "17.0",
  "com.googlecode.owasp-java-html-sanitizer" % "owasp-java-html-sanitizer" % "r239"
)
