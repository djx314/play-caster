organization := "net.scalax"

publishMavenStyle := true

licenses := Seq("MIT" -> url("http://www.opensource.org/licenses/bsd-license.php"))

homepage := Some(url("http://scalax.net/"))

publishTo := {
    Some("Sonatype Nexus" at "http://127.0.0.1:8081/repository/maven-releases/")
}

credentials += Credentials("Sonatype Nexus",
  "127.0.0.1",
  "admin",
  "admin123"
)

name := "play-caster"

version := "0.3.1-M3"

scalaVersion := "2.11.8"
scalacOptions ++= Seq("-feature", "-deprecation")

libraryDependencies ++= {
  val playVersion = "2.5.6"
  val circeVersion = "0.5.0-M3"
  //val shapelessVersion = "2.3.0"
  Seq(
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    //"com.chuusai" %% "shapeless" % shapelessVersion,
    "com.typesafe.play" %% "play" % playVersion % "provided",
    "org.slf4j" % "slf4j-simple" % "1.7.13" % "test",
    "org.scalatest" %% "scalatest" % "3.0.0-RC2" % "test",
    "com.typesafe.play" %% "play-test" % playVersion % "test",
    "com.typesafe.play" %% "play-ws" % playVersion % "test",
    "com.typesafe.play" %% "play-akka-http-server-experimental" % playVersion % "test"
  )
}
