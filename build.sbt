name := "stylekick-common"

version := "1.0"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2",
  "com.github.thiagolocatelli" % "parse4j" % "1.3",
  "net.codingwell" %% "scala-guice" % "4.0.0-beta4",
  "com.google.inject.extensions" % "guice-assistedinject" % "4.0-beta",
  "com.typesafe.play" %% "play-json" % "2.2.3",
  "com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2"
)


    