name := "akka-beginner"
version := "0.1"
scalaVersion := "2.12.8"


val akkaDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor",
  "com.typesafe.akka" %% "akka-stream",
  "com.typesafe.akka" %% "akka-testkit"
) map (_ % "2.5.19")

val akkaHttpDependencies = Seq(
  "com.typesafe.akka" %% "akka-http"
) map (_ % "10.1.6")

val apacheCxfCatalogVersion = Seq(
  "org.apache.cxf" % "cxf-rt-frontend-jaxws",
  "org.apache.cxf" % "cxf-rt-transports-http",
  "org.apache.cxf" % "cxf-rt-frontend-jaxrs",
  "org.apache.cxf" % "cxf-api",
  "org.apache.cxf" % "cxf-rt-rs-extension-providers"
) map (_ % "2.7.8")

libraryDependencies ++= akkaDependencies ++ akkaHttpDependencies ++ apacheCxfCatalogVersion ++ Seq(
  "org.scalatest" %% "scalatest" % "3.0.1" % "test"
)
