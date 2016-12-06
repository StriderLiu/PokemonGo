name := "PokemonGo"

version := "1.0"

lazy val `pokemongo` = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"

libraryDependencies ++= Seq(
  jdbc, cache, ws, specs2 % Test,
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "org.apache.spark" % "spark-core_2.11" % "2.0.1",
  "org.apache.spark" % "spark-mllib_2.11" % "2.0.1",
  "org.apache.spark" % "spark-hive_2.11" % "2.0.1"
)