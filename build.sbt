name := """play-silhouette-simple-slick"""
organization := "demo.matecki"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.12.2"

resolvers := ("Atlassian Releases" at "https://maven.atlassian.com/public/") +: resolvers.value

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.0" % Test
libraryDependencies += guice
libraryDependencies ++= Seq(
  "net.codingwell" %% "scala-guice" % "4.1.0"
  //"com.iheart" %% "ficus" % "1.4.1"
)

libraryDependencies ++= Seq(
  "com.mohiva" %% "play-silhouette" % "5.0.0-RC2",
  "com.mohiva" %% "play-silhouette-password-bcrypt" % "5.0.0-RC2",
  "com.mohiva" %% "play-silhouette-persistence" % "5.0.0-RC2",
  "com.mohiva" %% "play-silhouette-crypto-jca" % "5.0.0-RC2",
  "com.mohiva" %% "play-silhouette-testkit" % "5.0.0-RC2" % "test"
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "3.0.0",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.0"
)

libraryDependencies += "mysql" % "mysql-connector-java" % "6.0.6"
libraryDependencies += specs2 % Test
