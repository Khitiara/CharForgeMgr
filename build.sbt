name := "charforgemgr"

version := "1.0"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "com.google.api-client" % "google-api-client" % "1.22.0",
  "com.google.oauth-client" % "google-oauth-client-jetty" % "1.22.0",
  "com.google.apis" % "google-api-services-sheets" % "v4-rev482-1.22.0",
  "com.google.apis" % "google-api-services-drive" % "v2-rev276-1.21.0",
  "org.scalafx" %% "scalafx" % "8.0.102-R11",
  "com.typesafe.play" %% "play-json" % "2.6.1",
  "org.scalatra" %% "scalatra" % "2.5.1",
  "org.eclipse.jetty" % "jetty-webapp" % "9.2.19.v20160908",
  "javax.servlet" % "javax.servlet-api" % "3.1.0",
  "org.slf4j" % "slf4j-simple" % "1.7.25"
)

