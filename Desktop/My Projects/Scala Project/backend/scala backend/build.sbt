name := "gestion-universitaire-scala"
version := "1.0.0"
scalaVersion := "2.13.10"

val akkaVersion     = "2.8.0"
val akkaHttpVersion = "10.5.0"

libraryDependencies ++= Seq(
  // Base de données
  "org.postgresql"             %  "postgresql"           % "42.6.0",
  "com.typesafe"               %  "config"               % "1.4.2",
  "com.typesafe.slick"         %% "slick"                % "3.4.1",
  "com.typesafe.slick"         %% "slick-hikaricp"       % "3.4.1",

  // Akka
  "com.typesafe.akka"          %% "akka-actor"           % akkaVersion,
  "com.typesafe.akka"          %% "akka-actor-typed"     % akkaVersion,
  "com.typesafe.akka"          %% "akka-stream"          % akkaVersion,
  "com.typesafe.akka"          %% "akka-slf4j"           % akkaVersion,

  // Akka HTTP
  "com.typesafe.akka"          %% "akka-http"            % akkaHttpVersion,
  "com.typesafe.akka"          %% "akka-http-spray-json" % akkaHttpVersion,

  // Spark — 3.4.3 compatible Scala 2.13
  "org.apache.spark"           %% "spark-core"           % "3.4.3" % "compile",
  "org.apache.spark"           %% "spark-sql"            % "3.4.3" % "compile",

  // JSON
  "io.spray"                   %% "spray-json"           % "1.3.6",

  // CORS
  "ch.megard"                  %% "akka-http-cors"       % "1.2.0",

  // Logging
  "ch.qos.logback"             %  "logback-classic"      % "1.4.11",
  "com.typesafe.scala-logging" %% "scala-logging"        % "3.9.5"
)

excludeDependencies ++= Seq(
  ExclusionRule("org.slf4j", "slf4j-nop")
)

dependencyOverrides ++= Seq(
  "com.typesafe.akka" %% "akka-actor"       % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream"      % akkaVersion,
  "com.typesafe.akka" %% "akka-protobuf-v3" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j"       % akkaVersion
)

run / javaOptions += "--add-exports=java.base/sun.nio.ch=ALL-UNNAMED"
Compile / run / fork := true