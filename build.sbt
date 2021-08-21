name := """shoper"""
organization := "ru.trett"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.5"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies += guice
libraryDependencies += "org.webjars" % "jquery" % "3.6.0"
libraryDependencies += "com.adrianhurt" %% "play-bootstrap" % "1.6.1-P28-B4"
libraryDependencies += "org.webjars" % "bootstrap" % "4.6.0-1"
libraryDependencies += "org.webjars" % "font-awesome" % "5.15.3"
libraryDependencies += "com.h2database" % "h2" % "1.4.192"
libraryDependencies += "com.typesafe.play" %% "play-slick" % "5.0.0"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0"
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

Universal / packageName  := "shoper"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "ru.trett.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "ru.trett.binders._"
