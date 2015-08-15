import sbt._
import Keys._
import org.xarcher.caster.CustomSettings
import com.typesafe.sbt.SbtGit._

object caster extends Build {

  lazy val `play-caster` = (project in file("."))
  .settings(CustomSettings.customSettings: _*)
  .settings(
    name := "play-caster",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % "2.4.2",
      //jfc
      "io.circe" %% "circe-core" % "0.1.1",
      "io.circe" %% "circe-generic" % "0.1.1",
      "io.circe" %% "circe-jawn" % "0.1.1"
    )
  )

}
