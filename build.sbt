name := "akka-beginner"
version := "0.1"
scalaVersion := "2.12.4"


val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor",
  "com.typesafe.akka" %% "akka-testkit"
) map (_ % "2.5.11")

libraryDependencies ++= akkaDependencies ++ Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
