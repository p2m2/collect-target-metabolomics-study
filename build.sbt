name := """collect-target-metabolomics-study"""
organization := "fr.inrae.p2m2"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  guice,
    "com.github.p2m2" %% "p2m2tools" % "develop-SNAPSHOT",
    "com.typesafe.play" %% "play-slick" % "5.0.2",
    "com.typesafe.play" %% "play-slick-evolutions" % "5.0.2",
    "com.h2database" % "h2" % "2.1.212",
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
)

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "fr.inrae.p2m2.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "fr.inrae.p2m2.binders._"
Global / onChangedBuildSource := ReloadOnSourceChanges
