name := """ketsuco"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "mysql" % "mysql-connector-java" % "5.1.24",
  "org.scalikejdbc" %% "scalikejdbc"                  % "2.2.8",
  "org.scalikejdbc" %% "scalikejdbc-config"           % "2.2.8",
  "org.scalikejdbc" %% "scalikejdbc-play-initializer" % "2.4.1",
//  "com.typesafe.slick" %% "slick" % "2.1.0",
//  "com.typesafe.slick" %% "slick" % "3.0.0",
//  "com.typesafe.play" %% "play-slick" % "1.0.1",
//  "com.typesafe.play" %% "play-slick-evolutions" % "1.0.1",
  "nu.validator.htmlparser" % "htmlparser" % "1.4",
  evolutions,
  specs2 % Test
)

scalikejdbcSettings

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
