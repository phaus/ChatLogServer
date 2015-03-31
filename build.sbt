name := """ChatLogServer"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

maintainer in Docker := "Philipp Haußleiter <philipp.haussleiter@innoq.com>"

packageName in Docker := packageName.value

version in Docker := version.value

dockerExposedPorts in Docker := Seq(9000)

pipelineStages := Seq(rjs, uglify, digest, gzip)

includeFilter in uglify := GlobFilter("js/*.js")

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "mysql" % "mysql-connector-java" % "5.1.18",
  "com.google.guava" % "guava" % "17.0",
  "com.googlecode.owasp-java-html-sanitizer" % "owasp-java-html-sanitizer" % "r239",
  "org.webjars" % "webjars-play" % "2.1.0-1",
  "com.innoq.webjars" % "innoq-styles" % "0.1.1",
  "org.webjars" % "jquery" % "2.1.3",
  "org.webjars" % "jquery.sparkline" % "2.1.2"
)

resolvers += (
    "nexus.innoq.com snapshots" at "https://nexus.innoq.com/nexus/content/repositories/snapshots-private/"
)
