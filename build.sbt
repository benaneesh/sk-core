import com.typesafe.sbt.SbtStartScript

organization  := "com.stylekick"

name := "sk-core"

version       := "0.1"

scalaVersion  := "2.10.3"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.2.4"
  val sprayV = "1.2.1"
  Seq(
    "io.spray"            %   "spray-can"     % sprayV,
    "io.spray"            %   "spray-routing" % sprayV,
    "io.spray"            %   "spray-testkit" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.7" % "test"
  )
}

Revolver.settings

seq(SbtStartScript.startScriptForClassesSettings: _*)

lazy val `sk-core` = project.in(file(".")).aggregate(`stylekick-common`).dependsOn(`stylekick-common`)

lazy val `stylekick-common` = project.in(file("stylekick-common"))

