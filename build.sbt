organization := "play-caster"

name := "play-caster"

version := "0.2.0"

scalaVersion := "2.11.8"
//crossScalaVersions := Seq("2.10.6", "2.11.8")
scalacOptions ++= Seq("-feature", "-deprecation")

libraryDependencies ++= {
  val playVersion = "2.5.0"
  val circeVersion = "0.3.0"
  val shapelessVersion = "2.3.0"
  Seq(
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion exclude("com.chuusai", "shapeless" + scalaBinaryVersion.value),
    "io.circe" %% "circe-parser" % circeVersion,
    "com.chuusai" %% "shapeless" % shapelessVersion,
    "com.typesafe.play" %% "play" % playVersion % "provided",
    "org.slf4j" % "slf4j-simple" % "1.7.13" % "test",
    "org.scalatest" %% "scalatest" % "3.0.0-M16-SNAP1" % "test",
    "com.typesafe.play" %% "play-test" % playVersion % "test",
    "com.typesafe.play" %% "play-ws" % playVersion % "test",
    "com.typesafe.play" %% "play-akka-http-server-experimental" % playVersion % "test"
  )
}
/*libraryDependencies := {
  CrossVersion.partialVersion(scalaVersion.value) match {
    // if scala 2.11+ is used, quasiquotes are merged into scala-reflect
    case Some((2, scalaMajor)) if scalaMajor >= 11 =>
      libraryDependencies.value
    // in Scala 2.10, quasiquotes are provided by macro paradise
    case Some((2, 10)) =>
      libraryDependencies.value ++ Seq(
        compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
        "org.scalamacros" %% "quasiquotes" % "2.0.0" cross CrossVersion.binary)
  }
}*/